package com.offcn.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSouhu {
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testGetId() {
        System.out.println(restTemplate);
        String url = "http://v2.sohu.com/integration-api/mix/region/7551?page=2&size=40";
        String result = restTemplate.getForObject(url, String.class);
        //将字符串转成json对象
        JSONObject object = JSONObject.parseObject(result);
        //获取data中的json数组
        JSONArray jsonArray = object.getJSONArray("data");
        //将json数组中的每条数据都转成map集合
        List<HashMap> hashMaps = JSON.parseArray(jsonArray.toString(), HashMap.class);
        List<Integer> authorIdList = new ArrayList<>();
        //从map集合中取authorId
        for (HashMap<String, Integer> hashMap : hashMaps) {
            //获取作者id
            Integer authorId = hashMap.get("authorId");
            //System.out.println(authorId);
            authorIdList.add(authorId);
        }
        System.out.println(authorIdList.get(0));
        String url2 = "http://v2.sohu.com/author-page-api/author-articles/pc/" + authorIdList.get(0) + "?pNo=5";
        JSONObject autodata = restTemplate.getForObject(url2, JSONObject.class);
        System.out.println(autodata);
        JSONObject data = autodata.getJSONObject("data");
        JSONArray jsonArray1 = data.getJSONArray("pcArticleVOS");
        List<HashMap> list = JSON.parseArray(jsonArray1.toString(), HashMap.class);
        Integer id = null;
        Integer userId = null;
        for (HashMap<String, Integer> hashMap : list) {
            id = hashMap.get("id");
            userId = hashMap.get("userId");
            System.out.println("id:" + id);
            System.out.println("userId:" + userId);
        }
        //访问页面
        String htmlUrl = "http://www.sohu.com/a/" + id + "_" + userId;
        String html = restTemplate.getForObject(htmlUrl, String.class);
        System.out.println(html);
    }
}
