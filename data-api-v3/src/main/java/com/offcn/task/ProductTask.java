package com.offcn.task;


import com.alibaba.fastjson.JSON;
import com.offcn.consts.RedisProductkeyEnum;
import com.offcn.domain.Product;
import com.offcn.serivce.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProductTask {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductService productService;

    @Scheduled(cron = "0 0 0 * * ? ")
    public void refreshproductTask() {
        //清除redis缓存
        productService.clearAll();
        //爬取数据
        productService.reptitleCore();
        //存入数据库
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> members = setOps.members(RedisProductkeyEnum.SKUS.getValue());
        for (String member : members) {
            productService.add(JSON.parseObject(member, Product.class));
        }
    }


}
