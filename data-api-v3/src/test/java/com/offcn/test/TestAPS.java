package com.offcn.test;

import com.offcn.serivce.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestAPS {
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void test1() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = "https://p.3.cn/prices/mgets?skuIds=J_100008348542";
        httpHeaders.add("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> exchangeResult = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        System.out.println(exchangeResult.getBody());
        String str = restTemplate.getForObject(url, String.class);
        System.out.println(str);
    }
}
