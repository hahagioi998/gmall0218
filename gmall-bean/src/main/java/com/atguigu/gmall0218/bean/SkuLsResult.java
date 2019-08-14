package com.atguigu.gmall0218.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-31 16:04
 * @Description:es查询完返回的结果
 */
@Data
public class SkuLsResult implements Serializable {

    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;
}
