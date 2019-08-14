package com.atguigu.gmall0218.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-31 12:41
 * @Description:
 */
@Data
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    //热点度
    Long hotScore=0L;

    List<SkuLsAttrValue> skuAttrValueList;

}
