package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author qiyu
 * @create 2019-07-26 15:55
 * @Description:销售属性值表
 */
@Data
public class SpuSaleAttrValue implements Serializable{
    @Id
    @Column
    String id ;

    @Column
    String spuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrValueName;

    //当前销售属性值是否被选中
    @Transient
    String isChecked;
}
