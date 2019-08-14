package com.atguigu.gmall0218.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.manage.mapper.*;
import com.atguigu.gmall0218.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-24 12:33
 * @Description:
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;


    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;





    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
      //  System.out.println(catalog3Id);


        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));

    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入属性
        if(baseAttrInfo.getId() != null && baseAttrInfo.getId().length()>0){

            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //然后先删除平台属性值(这是修改步骤需要)
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        //然后再添加
        //循环遍历出来平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if(attrValueList != null && attrValueList.size()>0){
            for (BaseAttrValue attrValue : attrValueList) {
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);

            }

        }
    }

    /**
     * 查询所有的平台属性和平台属性值
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // baseAttrInfo.id = baseAttrValue.getAttrId();
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 需要将平台属性值集合放入平台属性中
        //  select * from baseAttrVallue where attrId = ?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;


    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    /**
     * 查询基本属性值
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    //添加平台属性
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
       // if(spuInfo.getId() == null || spuInfo.getId().length()==0){
            //添加平台属性
            spuInfoMapper.insertSelective(spuInfo);
        //}else {
         //   spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        //}


        //添加图片
//        SpuImage spuImage1 = new SpuImage();
//        spuImage1.setSpuId(spuInfo.getId());
//        spuImageMapper.delete(spuImage1);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);

            }
        }

        //销售属性删除
//        SpuSaleAttr spuSaleAttr1 = new SpuSaleAttr();
//        spuSaleAttr1.setSpuId(spuInfo.getId());
//        spuSaleAttrMapper.delete(spuSaleAttr1);

        //销售属性值删除
//        SpuSaleAttrValue spuSaleAttrValue1 = new SpuSaleAttrValue();
//        spuSaleAttrValue1.setSpuId(spuInfo.getId());
//        spuSaleAttrValueMapper.delete(spuSaleAttrValue1);



        //添加销售属性及销售属性值
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuImageList != null && spuImageList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrId(spuSaleAttr.getSaleAttrId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }


    }

    /**
     * 根据spuId查询所属的所有图片
     * @param
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {

        return spuImageMapper.select(spuImage);
    }

    /**
     * 查询平台属性集合
     * @param attrValueIdList
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String attrValueIds  = StringUtils.join(attrValueIdList.toArray(), ",");
        List<BaseAttrInfo> baseAttrInfoList  = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);

        return baseAttrInfoList;
    }

    /**
     * 销售属性
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        long aLong = Long.parseLong(spuId);

        return spuSaleAttrMapper.selectSpuSaleAttrList(aLong);
    }

    /**
     * 添加商品sku
     * @param skuInfo
     */
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //根据id开判断是添加还是修改
     //   if(skuInfo.getId() == null || skuInfo.getId().length() == 0){
            skuInfoMapper.insertSelective(skuInfo);
       // }else{
            //修改
        //    skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        //}

        //先删除再添加图片
//        SkuImage skuImage = new SkuImage();
//        skuImage.setSkuId(skuInfo.getId());
//        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size()>0){
            for (SkuImage image : skuImageList) {
                // skuId 必须赋值
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }

        //添加sku平台属性关联表
//        SkuAttrValue skuAttrValue = new SkuAttrValue();
//        skuAttrValue.setSkuId(skuInfo.getId());
//        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList !=null && skuAttrValueList.size() >0){
            for (SkuAttrValue attrValue : skuAttrValueList) {
                // skuId
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }

        //添加销售属性值
//        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
//        skuSaleAttrValue.setSkuId(skuInfo.getId());
//        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {

                // skuId
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }

        }


    }
}
