package com.offcn.domain;


import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "product")
public class Product implements Serializable {
    //主键
    @Id
    private Integer pid;
    //逻辑主键
    private String sku;
    private String title;
    private Double price;
    //商品规格参数
    private String name;
    // \a\a.jpg_星河银,b.jpg_亮黑
    private String img;

}
