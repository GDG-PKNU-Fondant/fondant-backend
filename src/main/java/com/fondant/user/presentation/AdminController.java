package com.fondant.user.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class AdminController {
    @GetMapping("/admin")
    public String adminTest() {
        return "Admin Controller";
    }
}
