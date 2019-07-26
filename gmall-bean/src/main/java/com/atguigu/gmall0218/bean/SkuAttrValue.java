package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author qiyu
 * @create 2019-07-26 20:14
 * @Description:sku平台属性值关联表
 */
@Data
public class SkuAttrValue implements Serializable {
    @Id
    @Column
    String id;

    @Column
    String attrId;

    @Column
    String valueId;

    @Column
    String skuId;
}
