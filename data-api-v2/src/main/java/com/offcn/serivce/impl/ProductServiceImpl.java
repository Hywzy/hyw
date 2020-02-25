package com.offcn.serivce.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.offcn.domain.Product;
import com.offcn.serivce.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    //爬取数据信息
    public List<Product> findByJd() {
        List<Product> list = new ArrayList<>();
        //从商品起始页获取详情页url
        String startUrl = "https://list.jd.com/list.html?cat=9987,653,655&page=1";
        String startResult = restTemplate.getForObject(startUrl, String.class);
        if (startResult == null) {
            return list;
        }
        Document document = Jsoup.parse(startResult);
        Elements elements = document.select(".gl-item");
        for (Element element : elements) {
            String sku = element.select(".j-sku-item").first().attr("data-sku");
            //System.out.println(sku);
            String detailUrl = "https://item.jd.com/" + sku + ".html";
            //请求详情页获取各个信息
            String detailResult = restTemplate.getForObject(detailUrl, String.class);
            if (detailResult == null) {
                throw new RuntimeException("请求商品查询页面失败");
            }

            //抓取图片,并将图片名改成我们规定的名字 \a\a.jpg_星河银,b.jpg_亮黑
            Document docImg = Jsoup.parse(detailResult);
            Elements imgs = docImg.select("#choose-attr-1 img");
            StringBuilder fullImgName = new StringBuilder();
            for (Element img : imgs) {
                String imgUrl = img.attr("src");
                //img14.360buyimg.com/n1/s450x450_jfs/t1/50018/39/8127/229510/5d5b5043E66769ff0/8907776f7bd66d57.jpg
                String imgName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                fullImgName.append(imgName).append("_").append(img.attr("alt")).append(",");
                byte[] bytes = restTemplate.getForObject("http:" + imgUrl.replace("/n9/s40x40_jfs/", "/n1/s450x450_jfs/"), byte[].class);
                //下载图片
                try {
                    FileUtils.writeByteArrayToFile(new File("D:\\pics\\" + imgName), bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //去掉最后的,
            String str = fullImgName.substring(0, fullImgName.length() - 1);
            //遍历所有规格的商品
            Elements guigeEle = docImg.select("#choose-attr-3 .item");
            for (Element item : guigeEle) {
                String url = "https://item.jd.com/" + item.attr("data-sku") + ".html";
                Product product = getProduct(url);
                product.setImg(str);
                log.info("抓取结束");
                list.add(product);
            }
        }
        return list;
    }

    //获取图片的sku,title,price,name
    public Product getProduct(String url) {
        Product product = new Product();
        log.info("当前正在爬取的地址是:" + url);
        String detailResult = restTemplate.getForObject(url, String.class);
        if (detailResult == null) {
            return product;
        }
        Document detailDoc1 = Jsoup.parse(detailResult);
        //抓取title
        String title = detailDoc1.select(".sku-name").text();
        String sku = detailDoc1.select("#choose-attr-1 .selected").first().attr("data-sku");
        //System.out.println(sku);
        String name = detailDoc1.select("#choose-attr-2 .selected").attr("data-value");
        //向接口发起请求，响应数据，接口由抓包工具抓来
        String resUrl = "https://p.3.cn/prices/mgets?skuIds=J_" + sku;
        String res = restTemplate.getForObject(resUrl, String.class);
        JSONArray jsonArray = JSON.parseArray(res);
        String priceStr = "0.0";
        if (jsonArray != null && jsonArray.size() > 0) {
            priceStr = JSON.parseObject(jsonArray.getString(0)).get("op") + "";
        }
        Double price = Double.parseDouble(priceStr);
        product.setSku(sku);
        product.setName(name);
        product.setTitle(title);
        product.setPrice(price);
        return product;
    }
}
