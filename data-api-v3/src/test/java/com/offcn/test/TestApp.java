package com.offcn.test;


import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApp {
    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void testGet() {

        String url = "https://item.jd.com/100003395445.html";
        //方式一，获取Json字符串
        //String str = restTemplate.getForObject(url,String.class);
        //方式二，获取包含Product实体对象的响应实体ResponseEntity对象，内容用getBody()获取
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        System.out.println(responseEntity.getBody());
        /*//方式三，
        HttpHeaders headers = new HttpHeaders();
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<String> exchangeResult = restTemplate.exchange(url, HttpMethod.GET,requestEntity,String.class);

        System.out.println(exchangeResult);*/
    }

    @Test
    public void testgetData() {
        String url = "https://list.jd.com/list.html?cat=9987,653,655";
        String result = restTemplate.getForObject(url, String.class);
        Document document = Jsoup.parse(result);
        Elements elements = document.select(".gl-item");

        for (Element element : elements) {
            Elements aTag = element.select(".p-name a");
            //详情链接
            String descUrl = aTag.attr("href");
            System.out.println("详情链接:" + descUrl);
            //标题
            String title = aTag.select("em").html();
            System.out.println("标题:" + title);
            //图片
            /*String imgPath = element.select(".p-scroll .ps-wrap .ps-item img").attr("src");
            System.out.println("图片:"+imgPath);
*/
            String img = element.select(".p-scroll .ps-wrap .ps-item a img").attr("src");
            String imgPath = "http:" + img.replace("/n9/", "/n1/");
            System.out.println("图片路径:" + imgPath);
            //存图
            byte[] bytes = restTemplate.getForObject(imgPath, byte[].class);
            try {
                FileUtils.writeByteArrayToFile(new File("D:\\pics\\" + System.currentTimeMillis() + ".jpg"), bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
