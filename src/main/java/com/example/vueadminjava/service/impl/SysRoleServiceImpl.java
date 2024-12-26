package com.example.vueadminjava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vueadminjava.entity.SysRole;
import com.example.vueadminjava.mapper.SysRoleMapper;
import com.example.vueadminjava.service.SysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public List<SysRole> listRolesByUserId(Long id) {
        List<SysRole> sysRoles = this.list(new QueryWrapper<SysRole>().inSql("id", "select role_id from sys_user_role where user_id = " + id));
        return sysRoles;
    }
}
