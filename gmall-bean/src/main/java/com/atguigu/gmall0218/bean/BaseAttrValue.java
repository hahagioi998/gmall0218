package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author qiyu
 * @create 2019-07-24 12:24
 * @Description:平台属性值表
 */
@Data
public class BaseAttrValue implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String valueName;
    @Column
    private String attrId;

    //历史参数路径
    @Transient
    private String urlParam;
}
