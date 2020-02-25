package com.offcn.domain;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
public class ProductVo {
    @Field
    private Integer pid;
    //逻辑主键
    private String sku;
    @Field("product_title")
    private String title;
    @Field("product_price")
    private Double price;
    //商品规格参数
    @Field("product_name")
    private String name;
    // \a\a.jpg_星河银,b.jpg_亮黑
    @Field("product_img")
    private String img;
    @Field("keyWords")
    private String keyWords;
}
