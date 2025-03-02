package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.robin.basis.model.user.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
