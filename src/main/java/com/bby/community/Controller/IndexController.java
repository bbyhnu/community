package com.bby.community.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")//什么都不输入，默认访问/ 根目录
    public String index(){return "index";}


}
