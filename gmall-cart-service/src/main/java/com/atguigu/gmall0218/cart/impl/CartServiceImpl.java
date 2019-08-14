package com.atguigu.gmall0218.cart.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.cart.constant.CartConst;
import com.atguigu.gmall0218.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.ItemManageService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author qiyu
 * @create 2019-08-03 17:58
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService{


        @Autowired
        private CartInfoMapper cartInfoMapper;

        @Reference
        private ItemManageService itemManageService;

        @Autowired
        private RedisUtil redisUtil;

    /**
     * 登录后添加商品到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
//        1、先检查该用户的购物车里是否已经有该商品
//        2、如果有商品，只要把对应商品的数量增加上去就可以，同时更新缓存
//        如果没有该商品，则把对应商品插入到购物车中，同时插入缓存

        //  先查cart中是否已经有该商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo selectCartInfo = cartInfoMapper.selectOne(cartInfo);
        if(selectCartInfo != null){//已经有该商品，更新购物车
            //更新商品数量
            selectCartInfo.setSkuNum(selectCartInfo.getSkuNum()+skuNum);
            //给实时价格赋值
            selectCartInfo.setSkuPrice(selectCartInfo.getCartPrice());

            cartInfoMapper.updateByPrimaryKeySelective(selectCartInfo);
        }else {//不存在该商品，
            //保存购物车，插入数据库
            SkuInfo skuInfo = itemManageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);

            // 插入数据库
            cartInfoMapper.insertSelective(cartInfo1);
            //if else只能走一个，selectCartInfo有值没值
            selectCartInfo = cartInfo1;

        }


        //更新redis缓存中的值
        // 构建key user:userid:cart
        String userCartKey =  CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;

        Jedis jedis = redisUtil.getJedis();
        //进行更新操作，要用hset，因为需要修改哪件商品，则反序列化哪一个商品即可！不需要全部反序列化
        String cartJson  = JSON.toJSONString(selectCartInfo);
        jedis.hset(userCartKey,skuId,cartJson);
        // 更新购物车在redis中的过期时间，设置过期时间与用户的过期时间一样
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        //获取用户的key过期时间
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        //关闭jedis
        jedis.close();
    }

    /**
     * 用户已经登录，从缓存中取值，如果缓存没有，加载数据库
     * 1、redis中取出来要进行反序列化
         2、redis的hash结构是无序的，要进行排序（可以用时间戳或者主键id，倒序排序）
         3、如果redis中没有要从数据库中查询，要连带把最新的价格也取出来，
            默认要显示最新价格而不是当时放入购物车的价格，如果考虑用户体验可以把两者的差价提示给用户。
         4、加载入缓存时一定要设定失效时间，保证和用户信息的失效时间一致即可。
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        // 从redis中取
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        //hvals:列出该hash集合的所有value
        List<String> cartJsons = jedis.hvals(userCartKey);

        if(cartJsons !=null && cartJsons.size()>0) {//缓存中有，从缓存中取
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);

            }

            //页面查看的时候进行排序，真实生产环境下用更新时间排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });

            return cartInfoList;

        }else {// 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息

            List<CartInfo> cartInfoList = loadCartCache(userId);
            return  cartInfoList;

        }

    }

    /**
     * 合并购物车:
     *      用数据库中的购物车列表与传递过来的cookie里的购物车列表循环匹配。
     *      能匹配上的数量相加
     *      匹配不上的插入到数据库中。
     *      最后重新加载缓存
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 循环开始匹配
        for (CartInfo cartInfoCK : cartListFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if(cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            // 数据库中没有购物车，则直接将cookie中购物车添加到数据库
            if(!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
            
        }
        // 重新在数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals(info.getSkuId())) {
                    //只有被勾选的才会被修改
                    if ("1".equals(info.getIsChecked())) {
                        cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    /**
     * 购物车中选中状态的变更.修改缓存
     *  思路：根据skuId从redis中取出来购物车的数据，反序列化，修改isChecked，再保存回redis中
     *         同时保存另外一个redis的key来存储用户选中的商品，方便结算页面使用
     *
     * @param skuId
     * @param isChecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        Jedis jedis = redisUtil.getJedis();

        //取得购物车中的数据
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        //将json串转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckJson);

        //新增到已选中的购物车
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CHECKED_KEY_SUFFIX;
        if("1".equals(isChecked)){
            jedis.hset(userCheckedKey,skuId,cartCheckJson);
        }else {
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    /**
     * 查询选中的购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        Jedis jedis = redisUtil.getJedis();
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;

        List<String> cartCheckedList  = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        if(cartCheckedList != null && cartCheckedList.size()>0){

            for (String cartJson : cartCheckedList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                newCartList.add(cartInfo);

            }
        }

        return newCartList;
    }


    /**
     * 在数据库中查询购物车列表
     * 要连带把最新的价格也取出来，
        默认要显示最新价格而不是当时放入购物车的价格，如果考虑用户体验可以把两者的差价提示给用户。
        加载入缓存时一定要设定失效时间，保证和用户信息的失效时间一致即可。
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }

        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());

        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }

        // 将java list - redis hash
        //将查询出来的数据列表放到redis中
        // hmset:批量设置hash的值
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }
}
