package com.example.vueadminjava.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vueadminjava.common.dto.PassDto;
import com.example.vueadminjava.common.lang.Const;
import com.example.vueadminjava.common.lang.Result;
import com.example.vueadminjava.entity.SysRole;
import com.example.vueadminjava.entity.SysUser;
import com.example.vueadminjava.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 我的公众号：MarkerHub
 * @since 2024-12-20
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;


    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result info(@PathVariable("id") Long id) {
        SysUser sysUser = sysUserService.getById(id);
        Assert.notNull(sysUser, "找不到该管理员");


        List<SysRole> roles = sysRoleService.listRolesByUserId(id);
        sysUser.setSysRoles(roles);
        return Result.succ(sysUser);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result list(String username) {
        Page<SysUser> pageData = sysUserService.page(getPage(), new QueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(username), "username", username));
        //此处需要显示role 名称 ，所以需要设置role 实体
        pageData.getRecords().forEach(item -> {
            item.setSysRoles(sysRoleService.listRolesByUserId(item.getId()));
        });
        return Result.succ(pageData);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sys:user:save')")
    public Result save(@Validated @RequestBody SysUser sysUser) {
        sysUser.setCreated(LocalDateTime.now());
        sysUser.setStatu(Const.STATUS_ON);

        //默认密码
        String password = passwordEncoder.encode(Const.DEFULT_PASSWORD);
        sysUser.setPassword(password);

        // 默认头像
        sysUser.setAvatar(Const.DEFULT_AVATAR);

        sysUserService.save(sysUser);
        return Result.succ(sysUser);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result update(@Validated @RequestBody SysUser sysUser) {
        sysUser.setUpdated(LocalDateTime.now());
        sysUserService.updateById(sysUser);
        return Result.succ(sysUser);
    }

    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result delete(@RequestBody Long[] ids) {
        sysUserService.removeByIds(Arrays.asList(ids));
        //删除关联表
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().in("user_id", ids));
        return Result.succ("");
    }

    @Transactional
    @PostMapping("/role/{userId}")
    @PreAuthorize("hasAuthority('sys:user:role')")
    public Result rolePerm(@PathVariable("userId") Long userId, @RequestBody Long[] roleIds) {

        List<SysUserRole> sysUserRoles = new ArrayList<>();
        Arrays.stream(roleIds).forEach(r -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(r);
            sysUserRoles.add(sysUserRole);
        });
        //先删除。后保存
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        sysUserRoleService.saveBatch(sysUserRoles);

        //删除缓存
        SysUser sysUser = sysUserService.getById(userId);
        sysUserService.clearUserAuthorityInfo(sysUser.getUsername());
        return Result.succ("");
    }

    @PostMapping("/repass")
    @PreAuthorize("hasAuthority('sys:user:repass')")
    public Result repass(@RequestBody Long userId) {
        SysUser sysUser = sysUserService.getById(userId);
        sysUser.setPassword(passwordEncoder.encode(Const.DEFULT_PASSWORD));
        sysUser.setUpdated(LocalDateTime.now());
        sysUserService.updateById(sysUser);
        return Result.succ("");
    }

    @PostMapping("/updatePass")
    public Result updatePass(@Validated @RequestBody PassDto passDto, Principal principal) {
        SysUser sysUser = sysUserService.getByUsername(principal.getName());

        boolean matches = passwordEncoder.matches(passDto.getCurrentPass(), sysUser.getPassword());
        if (!matches) {
            return Result.fail("旧密码不正确");
        }

        sysUser.setPassword(passwordEncoder.encode(passDto.getPassword()));
        sysUser.setUpdated(LocalDateTime.now());
        sysUserService.updateById(sysUser);

        return Result.succ("");
    }

}
