package com.offcn.serivce.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.offcn.consts.RedisProductkeyEnum;
import com.offcn.domain.Product;
import com.offcn.domain.ProductVo;
import com.offcn.mapper.ProductMapper;
import com.offcn.serivce.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SolrPageRequest;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    //爬取数据信息
    public void reptitleCore() {
        //从商品起始页获取详情页url
        String startUrl = "https://list.jd.com/list.html?cat=9987,653,655&page=1";
        Document document = sendWithGet(startUrl);
        //查询起始页列表，获取每个Product的sku值
        Elements elements = document.select(".gl-item");
        for (Element element : elements) {
            //提取详情页地址需要的sku值
            String sku = element.select(".j-sku-item").first().attr("data-sku");
            //去除重复数据
            if (isContainSku(sku)) {
                continue;
            }
            //通过sku拼接一个详情页地址，在详情页提取商品信息，封装商品对象
            String detailUrl = "https://item.jd.com/" + sku + ".html";
            Document docDesc = sendWithGet(detailUrl);
            //抓取图片,并将图片名改成我们规定的名字 \a\a.jpg_星河银,b.jpg_亮黑
            Elements imgs = docDesc.select("#choose-attr-1 img");
            if (imgs == null || imgs.size() <= 0) {
                continue;
            }
            //抓取数据存入redis
            listProductWithSku(docDesc);

        }


    }

    @Override
    public void clearAll() {
        redisTemplate.delete("product:sku");
        redisTemplate.delete("product:isTrue");
    }

    private Document sendWithGet(String url) {
        String result = restTemplate.getForObject(url, String.class);
        if (result == null) {
            throw new RuntimeException("请求页面失败" + url);
        }
        Document document = Jsoup.parse(result);
        return document;

    }

    /**
     * 遍历所有套装的商品，存入redis
     */

    private void listProductWithSku(Document docDesc) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        //获取套装中的产品
        Elements elements = docDesc.select("#choose-attr-3 .item");
        for (Element item : elements) {
            String sku = item.attr("data-sku");
            //拼接详情页的url
            String url = "https://item.jd.com/" + sku + ".html";
            Product product = getProduct(url);
            log.info("详情页面抓取完成: \n");
            //再次去重
            if (isContainSku(sku)) {
                continue;
            }
            //将商品转成Json串加入集合中保存
            setOps.add(RedisProductkeyEnum.SKUS.getValue(), JSON.toJSONString(product));
            //设置失效期 26小时后失效
            setOps.getOperations().expire(RedisProductkeyEnum.SKUS.getValue(), 26, TimeUnit.HOURS);


        }
    }

    private boolean isContainSku(String sku) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Boolean isMember = setOps.isMember(RedisProductkeyEnum.ISTRUE.getValue(), sku);
        boolean res = isMember == null ? false : isMember;
        //包含，跳过本次执行
        if (res) {
            return true;
        }
        //不包含，则存储sku的值
        setOps.add(RedisProductkeyEnum.ISTRUE.getValue(), sku);
        return false;
    }

    @Override
    public void add(Product product) {
        productMapper.insert(product);

    }

    @Override
    public List<Product> listByPage(int pageIndex, int pageSize) {
        //先从redis中查询数据并分页
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> members = setOps.members("product:skus");
        ZSetOperations<String, String> opsZset = redisTemplate.opsForZSet();
        for (String member : members) {
            opsZset.add("zset", member, 1);
        }
        int start = (pageIndex - 1) * pageSize;
        int end = start + pageSize - 1;
        Set<String> productskus = opsZset.range("zset", start, end);

        //分页的信息
        /*for (String s : productskus) {
            System.out.println(s);
        }*/
        PageHelper.startPage(pageIndex, pageSize);
        List<Product> list = productMapper.selectAll();

        //暂时缺少分页信息
        return list;
    }

    @Override
    public Product findBySku(String sku) {
        //先查缓存，缓存没有再查数据库
        ValueOperations<String, String> strOps = redisTemplate.opsForValue();
        String key = strOps.get("product:" + sku);
        //key为空到库里查 并存入redis
        if (key == null) {
            Product product = productMapper.selectOneByExample(sku);
            strOps.set(key, JSON.toJSONString(product), 26, TimeUnit.HOURS);
            return product;
        }
        //不为空
        return JSON.parseObject(key, Product.class);
    }

    @Override
    public Map<String, Object> searchByKeyWords(String keyWords, int pageNum, int pageSize) {
        //创建高亮查询器
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设置高亮选择器
        HighlightOptions options = new HighlightOptions();
        options.setSimplePrefix("<span style='color':'red'>");
        options.addField("product_title","product_name");
        options.setSimplePostfix("</span>");
        query.setHighlightOptions(options);
        //设置查询条件
        Criteria criteria = new Criteria("product_keywords").expression(keyWords);
        query.addCriteria(criteria);
        //设置分页
        SolrPageRequest request = new SolrPageRequest(pageNum-1,pageSize);
        query.setPageRequest(request);
        //执行查询
        HighlightPage<ProductVo> page = solrTemplate.queryForHighlightPage("collection1", query, ProductVo.class);
        List<HighlightEntry<ProductVo>> highlighted = page.getHighlighted();

        List<ProductVo> vos = new ArrayList<>();
        //遍历结果集
        for (HighlightEntry<ProductVo> productHighlightEntry : highlighted) {
           //获取高亮对象
            List<HighlightEntry.Highlight> highlights = productHighlightEntry.getHighlights();
           //获取高亮实体类
            ProductVo vo = productHighlightEntry.getEntity();
            for (HighlightEntry.Highlight highlight : highlights) {
                if("product_title".equals(highlight.getField())){
                    vo.setTitle(highlight.getSnipplets().get(0));
                }else if("product_name".equals(highlight.getField())){
                    vo.setName(highlight.getSnipplets().get(0));
                }
            }
            vos.add(vo);
        }
        //返回的有:高亮的数据内容ProductVo,分页信息
        Map<String,Object> map = new HashMap<>();
        map.put("content",vos);
        map.put("cuurentPageNum",page.getNumber()-1);
        map.put("totalPages",page.getTotalPages());
        return map;
    }

    //爬取详情页获取各个数据
    //获取图片的sku,title,price,name
    public Product getProduct(String url) {
        log.info("当前正在爬取的详情页:{}" + url);
        Product product = new Product();
        Document detailDoc = sendWithGet(url);
        //抓取sku,title,price,name,img
        String sku = detailDoc.select("#choose-attr-1 .selected").first().attr("data-sku");
        String title = detailDoc.select(".sku-name").text();
        String name = detailDoc.select("#choose-attr-2 .selected").attr("data-value");
        String resUrl = "https://p.3.cn/prices/mgets?skuIds=J_" + sku+"&pduid="+ new Random().nextInt();
        //System.out.println("resUrl:" + resUrl);
//        String res = restTemplate.getForObject(resUrl,String.class);
       HttpHeaders header = new HttpHeaders();
        header.add("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity(header);
        ResponseEntity<String> exchangeResult = restTemplate.exchange(resUrl, HttpMethod.GET, requestEntity, String.class);
        String jsonStr = exchangeResult.getBody();
        //System.out.println("jsonStr:"+jsonStr);
        //String jsonStr = restTemplate.getForObject(resUrl,String.class);
        JSONArray jsonArray = JSON.parseArray(jsonStr);
        //System.out.println("jsonArray"+jsonArray);
        String priceStr = "0.0";
        if (jsonArray != null && jsonArray.size() > 0) {
            priceStr = JSON.parseObject(jsonArray.getString(0)).get("op") + "";
        }
        Double price = Double.parseDouble(priceStr);

        String imgPath = detailDoc.select("#choose-attr-1 .selected img").attr("src");
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        StringBuilder fullImgName = new StringBuilder();
        fullImgName.append(imgName).append("_").append(detailDoc.select("#choose-attr-1 .selected img").attr("alt")).append(",");
        byte[] bytes = restTemplate.getForObject("http:" + imgPath.replace("/n9/s40x40_jfs/", "/n1/s450x450_jfs/"), byte[].class);
        //下载图片
        //去掉最后的,
        String str = fullImgName.substring(0, fullImgName.length() - 1);
        product.setImg(str);
        log.info("抓取结束");
        //图片抓取
        product.setSku(sku);
        product.setName(name);
        product.setTitle(title);
        product.setPrice(price);
        product.setImg(str);

        try {
            FileUtils.writeByteArrayToFile(new File("D:\\pics\\" + imgName + ".jpg"), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return product;
    }
}
