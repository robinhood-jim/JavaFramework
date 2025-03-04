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
    @Select("<script>select a.id as id,res_name as name,is_leaf as leafTag,url,status,a.pid as pid,permission,router_path as routerPath,seq_no as seqNo from t_sys_resource_info a,t_sys_resource_role_r b where a.id=b.res_id and a.status='1' and b.status='1' and b.role_id=#{roleId}  </script>")
    List<SysResourceDTO> queryByRole(@Param(value = "roleId") Long roleId);
}
