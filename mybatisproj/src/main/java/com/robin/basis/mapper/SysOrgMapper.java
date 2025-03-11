package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.model.system.SysOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysOrgMapper extends BaseMapper<SysOrg> {
    @Select("<script>select max(tree_code) as code from t_sys_org_info where status='1' and  pid=${pid}</script>")
    String getMaxOrgCodeByParent(@Param(value = "pid") Long pid);
    @Select("<script>select id,user_account as userAccount,user_name as userName,nick_name as nickName,phone_num as phoneNum,email as email,account_type as accountType,org_id as orgId from " +
            "t_sys_user_info a ${ew.customSqlSegment}"+" and id in (select b.user_id from t_sys_user_org_r b,t_sys_org_info c  where a.id=b.user_id and b.org_id=c.id"+
            "<when test='orgIds!=null'>"
            +" and b.org_id in "+
            "   <foreach item='id' index='index' collection='orgIds' open='(' separator=',' close=')'>" +
            "       #{id}" +
            "   </foreach>"
            +"</when>"
            +") </script>")
    IPage<SysUserDTO> selectUserInOrg(Page<SysUserQueryDTO> page, @Param(Constants.WRAPPER) QueryWrapper wrapper,@Param(value = "orgIds") List<Long> orgIds);
    @Select("<script>select id,user_account as userAccount,user_name as userName,nick_name as nickName,phone_num as phoneNum,email as email,account_type as accountType,org_id as orgId from " +
            "t_sys_user_info a ${ew.customSqlSegment}"+" and id not in (select b.user_id from t_sys_user_org_r b,t_sys_org_info c  where a.id=b.user_id and b.org_id=c.id"+
            "<when test='orgIds!=null'>"
            +" and b.org_id not in "+
            "   <foreach item='id' index='index' collection='orgIds' open='(' separator=',' close=')'>" +
            "       #{id}" +
            "   </foreach>"
            +"</when>"
            +") </script>")
    IPage<SysUserDTO> selectUserNotInOrg(Page<SysUserQueryDTO> page, @Param(Constants.WRAPPER) QueryWrapper wrapper,@Param(value = "orgIds") List<Long> orgIds);

}
