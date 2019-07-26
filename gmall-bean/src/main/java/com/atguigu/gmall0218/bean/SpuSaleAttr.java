package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-26 15:54
 * @Description:销售属性表
 */
@Data
public class SpuSaleAttr implements Serializable {
    @Id
    @Column
    String id ;

    @Column
    String spuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrName;

    //平台属性下的所有属性值
    @Transient
    List<SpuSaleAttrValue> spuSaleAttrValueList;
}
