package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.robin.basis.dto.EmployeeUserTenantDTO;
import com.robin.basis.model.user.TenantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TenantUserMapper extends BaseMapper<TenantUser> {
    @Select("<script>"+
            "select * from (select a.id,name,u.id as userId,r.tenant_id as tenantId from t_sys_employee a,t_sys_user_info u,t_tenant_user_r r where a.id=u.employee_id and r.user_id=u.id) t ${ew.customSqlSegment}"+"</script>")
    List<EmployeeUserTenantDTO> getTenantEmpUser(@Param(Constants.WRAPPER) QueryWrapper<EmployeeUserTenantDTO> wrapper);

    @Select("<script>"+
            "select * from (select a.id,name,u.id as userId from t_sys_employee a,t_sys_user_info u where a.id=u.employee_id and a.status='1' and b.status='1') t ${ew.customSqlSegment}"+"</script>")
    List<EmployeeUserTenantDTO> getEmployeeUser(@Param(Constants.WRAPPER) QueryWrapper<EmployeeUserTenantDTO> wrapper);
}
