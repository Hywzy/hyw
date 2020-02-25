package com.offcn.serivce;

import com.offcn.domain.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {
    //爬虫核心
    public void reptitleCore();

    //清空Redis
    public void clearAll();

    // public List<Product> findByJd();
    //向数据库中添加一条记录
    public void add(Product product);

    //分页
    List<Product> listByPage(int pageIndex, int pageSize);

    //根据id查找信息
    public Product findBySku(String sku);
    //查询solr高亮
    Map<String,Object> searchByKeyWords(String keyWords, int pageNum, int pageSize);
}
