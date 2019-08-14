package com.atguigu.gmall0218.list.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0218.bean.SkuAttrValue;
import com.atguigu.gmall0218.bean.SkuLsInfo;
import com.atguigu.gmall0218.bean.SkuLsParams;
import com.atguigu.gmall0218.bean.SkuLsResult;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.ListService;
import io.searchbox.client.JestClient;

import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

/**
 * @author qiyu
 * @create 2019-07-31 12:44
 * @Description:
 */
@Service
public class ListServiceImpl  implements ListService {

    @Autowired
    private JestClient jestClient;


    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 上架数据到es中
     * @param skuLsInfo
     */
    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 全文检索搜索
     *  1.  定义dsl 语句
         2.  定义动作
         3.  执行动作
         4.  获取结果集
     * @param skuLsParams
     * @return
     */
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        //制作查询语句
        String query = makeQueryStringForSearch(skuLsParams);

        //语句封装
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult searchResult=null;
        try {//执行查询语句染回的结果
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //把结果进行处理然后封装成想要的对象结果
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams,searchResult);

        return skuLsResult;
    }




    /**
     * 制作查询语句dsl 语句！
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //定义一个查询器，也就是大括号
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断平台属性值是否为空
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length()>0){
            // 创建match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            // 创建must
            boolQueryBuilder.must(matchQueryBuilder);
            // 设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();

            // 设置高亮的规则
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");
            // 将设置好的高亮对象放入查询器中

            searchSourceBuilder.highlight(highlighter);
        }

        // 判断平台属性值Id
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                // 创建term
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);

                // 创建filter 并添加term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }



        //判断3级分类id
        if(skuLsParams.getCatalog3Id() !=null && skuLsParams.getCatalog3Id().length()>0){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // query --bool
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置分页
        // from 从第几条开始查询
        // 10 条 每页 3  第一页 0 3 ，第二页 3,3 第三页 6，3
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        // 聚合
        // 创建一个对象 aggs:--terms
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");
        // "field": "skuAttrValueList.valueId"
        groupby_attr.field("skuAttrValueList.valueId");
        // aggs 放入查询器
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.out.println("query"+query);

        return query;
    }

    /**
     * 通过dsl 语句查询出来的结果
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        //声明一个对象用来封装好结果的对象来返回
        SkuLsResult skuLsResult = new SkuLsResult();

        //声明一个集合来存储SKuLsInfo 数据
//      设置  List<SkuLsInfo> skuLsInfoList;
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        //给集合赋值
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;

            //获取skuName的高亮
            if(hit.highlight != null && hit.highlight.size()>0){
                Map<String, List<String>> highlight = hit.highlight;
                List<String> stringList = highlight.get("skuName");

                //将高亮的skuName、显示
                String skuNameHI = stringList.get(0);
                skuLsInfo.setSkuName(skuNameHI);
            }
            skuLsInfoList.add(skuLsInfo);


        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);


        //    设置    long total;
        skuLsResult.setTotal(searchResult.getTotal());


        //  设置 long totalPages;
        // 如何计算总页数
        // 10 条数据 每页显示3条  几页？ 4
        // long totalPages = searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():(searchResult.getTotal()/skuLsParams.getPageSize())+1;
        long totalPages = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);



        // 设置 List<String> attrValueIdList;
        // 声明一个集合来存储平台属性值Id
        List<String> stringList = new ArrayList<>();
        // 获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        //循环遍历出来值然后放进集合中
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();

            stringList.add(valueId);
        }


        skuLsResult.setAttrValueIdList(stringList);

        return skuLsResult;
    }

    /**
     * 更新热度评分
     * @param skuId
     */
    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        int timesToEs = 10;

        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if(hotScore%timesToEs == 0){
            updateHotScore(skuId,  Math.round(hotScore));
        }

    }

    /**
     * 具体更新热度评分的实现方法
     * @param skuId
     * @param
     */
    private void updateHotScore(String skuId, long hotScore) {
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";
        Update update = new Update.Builder(updateJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

}
