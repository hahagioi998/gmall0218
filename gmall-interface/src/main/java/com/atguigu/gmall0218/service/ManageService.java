package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.*;

import java.util.List;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(String attrId);

    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    // 查询基本销售属性表
    List<BaseSaleAttr> getBaseSaleAttrList();

    //添加平台属性
    void saveSpuInfo(SpuInfo spuInfo);


    //销售属性
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 添加商品的sku
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 查询平台属性集合
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
