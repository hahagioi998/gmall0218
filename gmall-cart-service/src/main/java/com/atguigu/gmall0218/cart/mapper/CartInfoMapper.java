package com.atguigu.gmall0218.cart.mapper;

import com.atguigu.gmall0218.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-08-03 18:00
 * @Description:
 */
public interface CartInfoMapper extends Mapper<CartInfo>{
    /**
     * 查询该用户最新的购物车数据，关联着skuinfo表中的price
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
