package com.offcn.test;

import com.offcn.serivce.ProductService;
import com.offcn.task.ProductTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDay02 {

    @Autowired
    private ProductTask productTask;

    @Test
    public void testService() {
        productTask.productTask();
    }
}
