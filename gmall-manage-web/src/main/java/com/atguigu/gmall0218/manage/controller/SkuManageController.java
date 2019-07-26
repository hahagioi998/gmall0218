package com.atguigu.gmall0218.manage.controller;

import com.atguigu.gmall0218.bean.SpuImage;
import com.atguigu.gmall0218.service.ManageService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-26 18:39
 * @Description:TODO(这里用一句话来描述这个类的作用)
 */
@RestController
@CrossOrigin
public class SkuManageController {

    @com.alibaba.dubbo.config.annotation.Reference
    private ManageService manageService;

    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        if(!StringUtils.isEmpty(spuId)){
            return  manageService.getSpuImageList(spuId);
        }
        return null;
    }

}
