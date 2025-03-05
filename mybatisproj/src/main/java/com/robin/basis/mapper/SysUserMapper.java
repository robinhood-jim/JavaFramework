package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.model.user.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
