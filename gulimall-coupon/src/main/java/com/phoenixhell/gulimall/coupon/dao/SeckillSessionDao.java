package com.phoenixhell.gulimall.coupon.dao;

import com.phoenixhell.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 21:59:07
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}