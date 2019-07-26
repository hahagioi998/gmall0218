package com.atguigu.gmall0218.manage.mapper;

import com.atguigu.gmall0218.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    /**
     * 销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);
}
