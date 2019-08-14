package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.SkuLsInfo;
import com.atguigu.gmall0218.bean.SkuLsParams;
import com.atguigu.gmall0218.bean.SkuLsResult;

public interface ListService {

    /**
     * 添加数据到es中
     * @param skuLsInfo
     */
    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户的查询条件在es上查询出来结果
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 更新热度评分
     * @param skuId
     */
    public void incrHotScore(String skuId);
}
