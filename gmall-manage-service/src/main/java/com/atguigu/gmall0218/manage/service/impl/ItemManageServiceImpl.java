package com.atguigu.gmall0218.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.manage.constant.ManageConst;
import com.atguigu.gmall0218.manage.mapper.*;
import com.atguigu.gmall0218.service.ItemManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import com.atguigu.gmall0218.config.RedisUtil;

import javax.sound.midi.Soundbank;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author qiyu
 * @create 2019-07-27 16:04
 * @Description:
 */
@Service
public class ItemManageServiceImpl implements ItemManageService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;


//    @Override
//    public SkuInfo getSkuInfoPage(String skuId) {
//        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
//
//        //查询出图片
//        SkuImage skuImage = new SkuImage();
//        skuImage.setSkuId(skuId);
//        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
//
//        skuInfo.setSkuImageList(skuImageList);
//
//        return skuInfo;
//    }

    /**
     * 从缓存中获取skuinfo基本信息
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        //使用redisson解决分布式锁

        return getSkuInfoRedission(skuId);
    }

    /**
     * Redisson分布式锁解决缓存击穿问题
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedission(String skuId) {


        SkuInfo skuInfo = null;
        RLock lock = null;
        Jedis jedis = null;
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.122.134:6379");

            RedissonClient redissonClient = Redisson.create(config);
            //使用redisson 调用getLock
            lock = redissonClient.getLock("yourLock");
            //加锁
            lock.lock(10, TimeUnit.SECONDS);


            jedis = redisUtil.getJedis();
            // 定义key： 见名之意： sku：skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            //判断缓存中是否有数据，如果有就从缓存中取，如果没有就从数据库中取，然后放入缓存
            //判断缓存中key是否存在
            if(jedis.exists(skuKey)){
                //缓存中有,取出key的value
                String skuJson = jedis.get(skuKey);
                //将字符串转换为对象
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);

                return skuInfo;


            }else {
                //缓存中没有，查询数据库
                skuInfo = getSkuInfoDB(skuId);

                //放入缓存,并设置过期时间
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));

                return skuInfo;
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null){
                jedis.close();
            }
            if(lock != null){
                lock.unlock();
            }
        }

        return getSkuInfoDB(skuId);
    }

    /**
     * jedis分布式锁
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoJedis(String skuId) {
        Jedis jedis = null;
        SkuInfo skuInfo = null;
        try {
            jedis = redisUtil.getJedis();
            //生成key
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            //直接获取然后判断
            String skuJson  = jedis.get(skuInfoKey);

            if(skuJson == null || skuJson.length() == 0){
                //没有命中缓存
                //没有数据。需要进行加锁，取出数据，然后放进缓存中
                System.out.println("没有命中缓存l");
                //定义key user:skuId:lock
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;

                //生成锁
                String lockKey  = jedis.set(skuLockKey,"OK","NX","PX",ManageConst.SKULOCK_EXPIRE_PX);

                if("OK".equals(lockKey)){
                    //获取锁
                    //从数据库中获取数据
                    skuInfo = getSkuInfoDB(skuId);

                    // 将对象转换成字符串
                    String skuRedisStr  = JSON.toJSONString(skuInfo);
                    //放进缓存中
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);

                    //删除这个锁
                    jedis.del(skuLockKey);


                    //返回数据
                    return skuInfo;
                }else {
                    //没有获取锁
                    System.out.println("等待！");
                    // 等待
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }


            }else {//缓存中有数据
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);

                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis == null){
                //关闭jedis
                jedis.close();
            }
        }

        // 从数据库返回数据
        return getSkuInfoDB(skuId);
    }

    /**
     * 不用的方法
     * @param skuId
     * @return
     */
    private SkuInfo toDogetSkuInfo(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        SkuInfo skuInfo = null;
        String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
        //判断一下缓存中是否有这个key
        if(jedis.exists(skuInfoKey)){
            //取出数据
            String skuInfoJson = jedis.get(skuInfoKey);

            //将数据转换成对象
            if(skuInfoJson != null && skuInfoJson.length()!=0){
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);

            }
        }else{


            //从数据库中取得数据

             skuInfo = getSkuInfoDB(skuId);

            //将数据放进缓存中
            String jsonString = JSON.toJSONString(skuInfo);
            jedis.setex(skuInfoKey,ManageConst.SKULOCK_EXPIRE_PX,jsonString);
        }
        //关闭jedis
        jedis.close();


        return skuInfo;
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        //查询出图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        skuInfo.setSkuImageList(skuImageList);

        //查询出平台属性值信息
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);


        return skuInfo;
    }

    /**
     * spu，sku信息
     * @param skuInfo
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    /**
     * 根据spuId查询销售属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }
}
