package com.offcn.test;

import com.alibaba.fastjson.JSON;
import com.offcn.Dataapi2Application;
import com.offcn.domain.Product;
import com.offcn.mapper.ProductMapper;
import com.offcn.serivce.ProductService;
import com.offcn.task.ProductTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Dataapi2Application.class})
public class TestVersion {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductTask productTask;
    @Autowired
    private ProductService productService;

    @Test
    public void test() {
        productTask.refreshproductTask();

        //存进数据库
       /* SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> members = setOps.members("product:skus");
        //System.out.println("members:"+members);
        for (String member : members) {
            System.out.println("member:"+member);
            Product product = JSON.parseObject(member, Product.class);
            System.out.println("product:"+product);
            productService.add(product);
        }*/
    }

    @Test
    //redis分页
    public void redisPageTest() {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> members = setOps.members("product:skus");
        ZSetOperations<String, String> opsZset = redisTemplate.opsForZSet();
        for (String member : members) {
            opsZset.add("zset", member, 1);
        }
        int pageSize = 2;
        int pageIndex = 2;
        int start = (pageIndex - 1) * pageSize;
        int end = start + pageSize - 1;
        Set<String> productskus = opsZset.range("zset", start, end);
        for (String s : productskus) {
            System.out.println(s);
        }
    }

}
