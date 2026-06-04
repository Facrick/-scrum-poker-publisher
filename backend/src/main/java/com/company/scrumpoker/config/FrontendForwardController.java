package com.company.scrumpoker.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendForwardController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/register",
            "/room/{path:[^\\.]*}",
            "/room/{path:[^\\.]*}/join",
            "/404"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
