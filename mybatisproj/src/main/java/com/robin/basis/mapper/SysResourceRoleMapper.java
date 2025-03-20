package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.model.user.SysResourceRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysResourceRoleMapper extends BaseMapper<SysResourceRole> {
    @Select("<script>"
            +"select a.id,res_name as resName,res_code as resCode,icon,res_type as resType,router_path as routerPath,pid,is_leaf as leafTag,icon,component_name as componentName "
            +"from t_sys_resource_info a,t_sys_resource_role_r b where a.id=b.res_id and a.status='1' and b.status='1' and b.role_id=${roleId}"
            +"</script>")
    List<SysResourceDTO> queryResourceByRole(@Param(value = "roleId") Long roleId);

}
