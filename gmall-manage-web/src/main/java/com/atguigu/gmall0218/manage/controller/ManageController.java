package com.atguigu.gmall0218.manage.controller;

import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.ManageService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-24 12:38
 * @Description:TODO(这里用一句话来描述这个类的作用)
 */
@RestController
@CrossOrigin
public class ManageController {
    @com.alibaba.dubbo.config.annotation.Reference
    private ManageService manageService;


    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    /**
     * 添加或者修改平台属性
     * @param baseAttrInfo
     */
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        // 调用服务层做保存或修改方法
         manageService.saveAttrInfo(baseAttrInfo);
    }

    /**
     * 查询平台属性值
     * @param attrId
     * @return
     */
    @RequestMapping("getAttrValueList")
    public  List<BaseAttrValue> getAttrValueList(String attrId){
        // 先通过attrId 查询平台属性 select * from baseAttrInfo where id = attrId
        BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);
        // 返回平台属性中的平台属性值集合baseAttrInfo.getAttrValueList();
        return baseAttrInfo.getAttrValueList();

    }

    /**
     * 查询基本销售属性
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    /**
     * 查询所有的销售属性
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        System.out.println("*************spuId"+spuId);
        return manageService.getSpuSaleAttrList(spuId);
    }

    /**
     * 添加商品的sku
     */
    @RequestMapping("saveSkuInfo")
    public void  saveSkuInfo(@RequestBody  SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }

}

