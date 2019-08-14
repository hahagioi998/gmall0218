package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.bean.SkuSaleAttrValue;
import com.atguigu.gmall0218.bean.SpuSaleAttr;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-27 15:20
 * @Description:
 */
public interface ItemManageService {
    /**
     * 前台页面
     * @param skuId
     * @return
     */
    //SkuInfo getSkuInfoPage(String skuId);

    /**
     * 查询skuinfo基本信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 存储spu，sku数据
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);


    /**
     * 根据spuId查询销售属性值
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
