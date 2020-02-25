package com.offcn.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductController {
    @RequestMapping("/toIndex")
    public String toIndex(){
        return "index.html";
    }
}
