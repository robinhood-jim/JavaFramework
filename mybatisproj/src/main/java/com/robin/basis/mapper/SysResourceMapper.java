package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.vo.SysResourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysResourceMapper extends BaseMapper<SysResource> {
    @Select("<script>select a.id as id,res_name as name,is_leaf as leafTag,url,status,a.pid as pid,permission,router_path as routerPath,seq_no as seqNo from t_sys_resource_info a,t_sys_resource_role_r b where a.id=b.res_id and a.status='1' and b.status='1' and b.role_id=${roleId}  </script>")
    List<SysResourceDTO> queryByRole(@Param(value = "roleId") Long roleId);
    @Select("<script>"
            +"select id,res_type as resType,res_name as resName,url,is_leaf as leafTag,res_code as resCode,pid,seq_no as seqNo,assign_type as assignType,is_leaf as leafTag,icon,router_path as routerPath,component_name as componentName "
            +"from (select a.*,0 as assign_type from t_sys_resource_info a,t_sys_resource_role_r b,t_sys_role_info c,t_sys_user_role_r d where a.status='1' and b.status='1' and a.ID=b.RES_ID and b.role_id=c.id and c.id=d.role_id "
            +"<when test='tenantId!=null'>"
            +" and  a.tenant_id=#{tenantId}"
            +"</when>"
            +" and d.user_id=${userId} and a.STATUS='1' and b.status='1' " +
            "  union select c.*,d.assign_type from t_sys_resource_info c,t_sys_resource_user_r d where c.id=d.res_id and d.USER_ID=#{userId} and c.STATUS='1' and d.status='1' )e order by res_code,seq_no,assign_type desc"
            +"</script>")
    List<SysResourceDTO> queryUserPermission(@Param(value = "userId") Long userId,@Param(value = "tenantId") Long tenantId);
}
