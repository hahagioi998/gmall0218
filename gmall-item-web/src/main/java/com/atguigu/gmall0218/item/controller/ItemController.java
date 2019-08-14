package com.atguigu.gmall0218.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.bean.SkuSaleAttrValue;
import com.atguigu.gmall0218.bean.SpuSaleAttr;
import com.atguigu.gmall0218.handler.LoginRequire;
import com.atguigu.gmall0218.service.ItemManageService;

import com.atguigu.gmall0218.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qiyu
 * @create 2019-07-27 13:15
 * @Description:
 */
@Controller
public class ItemController {

        @Reference
        private ItemManageService itemManageService;

        @Reference
        private ListService listService;
    /**
     * 查询销售属性
     * @param skuId
     * @param model
     * @return
     */
//    @RequestMapping("{skuId}.html")
//    public String skuInfoPage(@PathVariable(value = "skuId") String skuId, Model model){
//
//        SkuInfo skuInfo = itemManageService.getSkuInfoPage(skuId);
//        model.addAttribute("skuInfo",skuInfo);
//
//        return "item";
//    }

    //@LoginRequire
    @RequestMapping("{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId") String skuId,Model model){
        // 封装基本的skuInfo信息
        SkuInfo skuInfo = itemManageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);


        //查询Spu sku数据
        List<SpuSaleAttr> saleAttrList = itemManageService.getSpuSaleAttrListCheckBySku(skuInfo);
        model.addAttribute("saleAttrList",saleAttrList);


        //展示当前spu的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = itemManageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        //把列表变换成 valueid1|valueid2|valueid3 ：skuId  的 哈希表 用于在页面中定位查寻
        String valueIdsKey="";
        Map<String,String> skuSaleAttrValueMap = new HashMap<>();

        if(skuSaleAttrValueListBySpu != null && skuSaleAttrValueListBySpu.size()>0){
            for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);

                if(valueIdsKey.length() != 0){
                    valueIdsKey= valueIdsKey+"|";
                }
                //进行拼接
                valueIdsKey = valueIdsKey+skuSaleAttrValue.getSaleAttrValueId();

               //满足条件将其放进map中，然后清空继续进行拼接下一个
                if((i+1) == skuSaleAttrValueListBySpu.size() ||
                        !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                    skuSaleAttrValueMap.put(valueIdsKey,skuSaleAttrValue.getSkuId());
                    valueIdsKey = "";
                }


            }



        }
        //把map变成json串
        String valuesSkuJson  = JSON.toJSONString(skuSaleAttrValueMap);
       // System.out.println("*************************"+valuesSkuJson);
        model.addAttribute("valuesSkuJson",valuesSkuJson);

        //热点度更改，最终应由异步方式调用
        listService.incrHotScore(skuId);

        return "item";
    }


}
