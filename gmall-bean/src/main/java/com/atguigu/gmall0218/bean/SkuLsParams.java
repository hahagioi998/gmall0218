package com.atguigu.gmall0218.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qiyu
 * @create 2019-07-31 16:03
 * @Description:用户传过来 的参数封装成的VO
 */
@Data
public class SkuLsParams implements Serializable {
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;
}
