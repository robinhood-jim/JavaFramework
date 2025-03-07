package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.model.user.TenantUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantUserMapper extends BaseMapper<TenantUser> {
}
