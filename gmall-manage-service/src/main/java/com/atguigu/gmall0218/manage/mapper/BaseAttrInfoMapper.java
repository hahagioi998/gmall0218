package com.atguigu.gmall0218.manage.mapper;

import com.atguigu.gmall0218.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    // 根据三级分类id查询属性表
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);

    /**
     * 根据平台属性值查询平台属性集合
     * @Param注解否则${ }无法识别。
     * @param attrValueIds
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("attrValueIds")String attrValueIds);
}
