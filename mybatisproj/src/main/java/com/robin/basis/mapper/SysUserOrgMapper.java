package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.model.user.SysUserOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserOrgMapper extends BaseMapper<SysUserOrg> {
    @Select("<script>"
            +"select a.id as userId,a.org_id as orgId,c.org_name as orgName,c.org_code as orgCode from t_sys_user_org_r a,t_sys_org_info c "
            +" where a.status='1' and c.status='1' and a.org_id=c.id and a.user_id=${userId}"
            +"</script>")
    List<Map<String,Object>> getUserOrgs(@Param(value = "userId") Long userId);
}
