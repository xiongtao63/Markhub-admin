package com.example.vueadminjava.controller;

import com.example.vueadminjava.common.lang.Result;
import com.example.vueadminjava.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private SysUserService sysUserService;

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/test")
    public Result test(){
        return Result.succ(sysUserService.list());
    }

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/test/pass")
    public Result pass(){
        String password = passwordEncoder.encode("111111");
        boolean matches = passwordEncoder.matches("111111", password);
        System.out.println("匹配是否正确"+matches);
        return Result.succ(password);
    }
}
