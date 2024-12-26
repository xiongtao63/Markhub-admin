package com.example.vueadminjava.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vueadminjava.common.lang.Const;
import com.example.vueadminjava.common.lang.Result;
import com.example.vueadminjava.entity.SysMenu;
import com.example.vueadminjava.entity.SysRole;
import com.example.vueadminjava.entity.SysRoleMenu;
import com.example.vueadminjava.entity.SysUserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 我的公众号：MarkerHub
 * @since 2024-12-20
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends BaseController {

    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result info(@PathVariable(name = "id") Long id){
        SysRole sysRole = sysRoleService.getById(id);
        //获取角色相关联的菜单id
        List<SysRoleMenu> roleMenus  = sysRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        List<Long> menuIds = roleMenus.stream().map(p -> p.getMenuId()).collect(Collectors.toList());

        sysRole.setMenuIds(menuIds);

        return Result.succ(sysRole);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result list(String name){
        Page<SysRole> pageData = sysRoleService.page(getPage(),
                new QueryWrapper<SysRole>().like(StrUtil.isNotBlank(name), "name", name));
        return Result.succ(pageData);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sys:role:save')")
    public Result save(@Validated @RequestBody SysRole sysRole){
        sysRole.setCreated(LocalDateTime.now());
        sysRole.setStatu(Const.STATUS_ON);
        sysRoleService.save(sysRole);
        return Result.succ(sysRole);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:role:update')")
    public Result update(@Validated @RequestBody SysRole sysRole){
        sysRole.setUpdated(LocalDateTime.now());
        sysRoleService.updateById(sysRole);
        //更新缓存
        sysUserService.clearUserAuthorityInfoByRoleId(sysRole.getId());
        return Result.succ(sysRole);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @Transactional
    public Result delete(@RequestBody Long[] ids){
        sysRoleService.removeByIds(Arrays.asList(ids));
        //删除中间表
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().in("role_id", ids));
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().in("role_id", ids));
        Arrays.stream(ids).forEach(id -> {
            // 更新缓存
            sysUserService.clearUserAuthorityInfoByRoleId(id);
        });
        return Result.succ("");
    }
    @Transactional
    @PostMapping("/perm/{roleId}")
    @PreAuthorize("hasAuthority('sys:role:perm')")
    public Result perm(@PathVariable("roleId") Long roleId, @RequestBody Long[] menuIds){

        List<SysRoleMenu> roleMenus = new ArrayList<>();
         Arrays.stream(menuIds).forEach(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);

        });
         //删除原来的并保存
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        sysRoleMenuService.saveBatch(roleMenus);

        // 删除缓存
        sysUserService.clearUserAuthorityInfoByRoleId(roleId);
        return Result.succ("");
    }


}
