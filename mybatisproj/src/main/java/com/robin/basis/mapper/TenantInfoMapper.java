package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.model.system.TenantInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TenantInfoMapper extends BaseMapper<TenantInfo> {
    @Select("<script>"
            +"select a.user_id as userId,b.id as id,b.tenant_name as tenantName,b.tenant_code as tenantCode,b.org_id as orgId,c.org_name as orgName,c.org_code as orgCode,a.type,b.logo from t_tenant_user_r a,t_tenant_info b,t_sys_org_info c "
            +" where a.status='1' and b.status='1' and c.status='1' and a.TENANT_ID=b.id and c.id=b.org_id and a.user_id=${userId}"
    +"</script>")
    List<TenantInfoDTO> queryTenantByUser(@Param(value = "userId") Long userId);


}
