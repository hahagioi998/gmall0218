package com.atguigu.gmall0218.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.SpuInfo;


import com.atguigu.gmall0218.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-26 11:11
 * @Description:
 */
@RestController
@CrossOrigin
public class SpuInfoController {

    @Reference
    private ManageService manageService;

    /**
     *  平台属性
     * @param spuInfo
     * @return
     */
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){

        return manageService.getSpuInfoList(spuInfo);


    }


    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        if(spuInfo != null){
            manageService.saveSpuInfo(spuInfo);
        }

    }


}
