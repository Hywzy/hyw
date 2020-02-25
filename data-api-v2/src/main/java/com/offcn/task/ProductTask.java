package com.offcn.task;


import com.offcn.serivce.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class ProductTask {
    @Autowired
    private ProductService productService;

    @Scheduled(cron = "0 0 0 * * ? ")
    public void productTask() {
        productService.findByJd();
    }
}
