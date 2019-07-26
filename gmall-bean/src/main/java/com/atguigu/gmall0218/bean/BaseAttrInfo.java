package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-24 12:24
 * @Description:平台属性表
 */
@Data
public class BaseAttrInfo implements Serializable{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)//主键自增
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    //平台属性值
    @Transient//数据库表中没有。但是对应的实体类中可以有下面的属性
    private List<BaseAttrValue> attrValueList;


}
