package com.example.vueadminjava.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vueadminjava.common.dto.SysMenuDto;
import com.example.vueadminjava.entity.SysMenu;
import com.example.vueadminjava.entity.SysUser;
import com.example.vueadminjava.mapper.SysMenuMapper;
import com.example.vueadminjava.mapper.SysUserMapper;
import com.example.vueadminjava.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vueadminjava.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 我的公众号：MarkerHub
 * @since 2024-12-20
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysUserMapper sysUserMapper;


    @Override
    public List<SysMenuDto> getCurrentUserNav() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser sysUser = sysUserService.getByUsername(username);

        List<Long> menuIds  = sysUserMapper.getNavMenuIds(sysUser.getId());
        List<SysMenu> sysMenus = this.listByIds(menuIds);

        // 转树状结构
        List<SysMenu> menuTree = buildTreeMenu(sysMenus);

        // 实体转DTO
        return convert(menuTree);

    }

    @Override
    public List<SysMenu> tree() {
        List<SysMenu> sysMenus = this.list(new QueryWrapper<SysMenu>().orderByAsc("orderNum"));
        // 转成树状结构
        return buildTreeMenu(sysMenus);
    }

    private List<SysMenu> buildTreeMenu(List<SysMenu> menus) {
        List<SysMenu> finalMenus = new ArrayList<>();

        // 先各自寻找到各自的孩子

        for (SysMenu menu : menus) {

            for (SysMenu e : menus) {
                if (menu.getId().equals(e.getParentId())) {
                    menu.getChildren().add(e);
                }
            }

            // 提取出父节点
            if (menu.getParentId() == 0L) {
                finalMenus.add(menu);
            }
        }
        System.out.println(JSONUtil.toJsonStr(finalMenus));
        return finalMenus;
    }

    private List<SysMenuDto> convert(List<SysMenu> menuTree) {
        List<SysMenuDto> menuDtos = new ArrayList<>();
        menuTree.forEach(m -> {
            SysMenuDto dto = new SysMenuDto();

            dto.setId(m.getId());
            dto.setName(m.getPerms());
            dto.setTitle(m.getName());
            dto.setComponent(m.getComponent());
            dto.setPath(m.getPath());

            if (m.getChildren().size() > 0) {

                // 子节点调用当前方法进行再次转换
                dto.setChildren(convert(m.getChildren()));
            }

            menuDtos.add(dto);
        });
        return menuDtos;
    }
}
