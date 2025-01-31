package com.example.vueadminjava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vueadminjava.service.*;
import com.example.vueadminjava.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

public class BaseController {
    @Autowired
    HttpServletRequest req;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysRoleService sysRoleService;

    @Autowired
    SysMenuService sysMenuService;

    @Autowired
    SysUserRoleService sysUserRoleService;

    @Autowired
    SysRoleMenuService sysRoleMenuService;

    public Page getPage(){
        int page = ServletRequestUtils.getIntParameter(req, "page", 1);
        int size = ServletRequestUtils.getIntParameter(req, "size", 10);
        return new Page<>(page, size);
    }
}
