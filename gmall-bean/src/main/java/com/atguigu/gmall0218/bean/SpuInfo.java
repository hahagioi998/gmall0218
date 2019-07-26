package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-26 11:08
 * @Description:spu一组易检索，可复用的标准化信息集合
 */
@Data
public class SpuInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuName;

    @Column
    private String description;

    @Column
    private  String catalog3Id;

    //平台属性集合
    @Transient
    private List<SpuSaleAttr> spuSaleAttrList;
    //图片集合
    @Transient
    private List<SpuImage> spuImageList;
}
