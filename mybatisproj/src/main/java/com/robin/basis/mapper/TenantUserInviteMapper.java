package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.model.user.TenantUserInvite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantUserInviteMapper extends BaseMapper<TenantUserInvite> {
}
