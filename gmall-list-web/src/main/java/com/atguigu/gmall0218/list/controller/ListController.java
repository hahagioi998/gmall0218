package com.atguigu.gmall0218.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.ListService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-31 17:53
 * @Description:
 */
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams,Model model){

        //分页放在上面是因为要用这个值先去es查然后才能出效果
        skuLsParams.setPageSize(2);

        //sku信息
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        System.out.println("skuLsResult"+skuLsResult);

        //平台属性集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);


        // 已选的属性值列表
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();

        String urlParam = makeUrlParam(skuLsParams);


        //
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            //平台属性
            BaseAttrInfo baseAttrInfo = iterator.next();
            //平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

            for (BaseAttrValue baseAttrValue : attrValueList) {

                if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值比较
                        if(valueId.equals(baseAttrValue.getId())){
                            //移除url上一样的valueId
                            iterator.remove();

                            //构造面包屑列表
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            // 去除重复数据
                            String makeUrlParam  = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValuesList.add(baseAttrValueSelected);

                        }
                    }
                }

            }



        }

        //返回查询的信息
        model.addAttribute("totalPages",skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("baseAttrValuesList",baseAttrValuesList);
        model.addAttribute("keyword",skuLsParams.getKeyword());
        model.addAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());
        model.addAttribute("attrList",attrList);
        model.addAttribute("urlParam",urlParam);

        System.out.println("*********baseAttrValuesList+"+baseAttrValuesList);
        System.out.println("*********keyword"+skuLsParams.getKeyword());
        System.out.println("*********skuLsInfoList"+skuLsResult.getSkuLsInfoList());
        System.out.println("*********attrList"+attrList);
        System.out.println("*********urlParam"+urlParam);
        // return JSON.toJSONString(skuLsResult);
        return "list";
    }

    //
    //添加了面包屑功能
    private String makeUrlParam(SkuLsParams skuLsParams,String...excludeValueIds) {
        //拼接条件,点击属性时，要把上次查询的内容也带上，即带上历史参数
        String urlParam = "";



        //判断是否有上一次的keyword然后拼接
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }

        //拼接三级分类id
        if(skuLsParams.getCatalog3Id()!= null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }

            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }


        //拼接平台属性值valueId参数
        if(skuLsParams.getValueId() !=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueIds != null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
                        // 如果写了break；其他条件则无法拼接！
                        continue;
                    }



                }


                if (urlParam.length()>0){
                    urlParam+="&";
                }

                urlParam+="valueId="+valueId;
            }
        }

        return  urlParam;

    }

    //没加面包屑之前
    private String makeUrlParamTODO(SkuLsParams skuLsParams) {
        //拼接条件,点击属性时，要把上次查询的内容也带上，即带上历史参数

        String urlParam = "";
        //判断是否有上一次的keyword然后拼接
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }

        //拼接三级分类id
        if(skuLsParams.getCatalog3Id()!= null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }

            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }


        //拼接平台属性值valueId参数
        if(skuLsParams.getValueId() !=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];

                if (urlParam.length()>0){
                    urlParam+="&";
                }

                urlParam+="valueId="+valueId;
            }
        }

        return  urlParam;

    }

}
