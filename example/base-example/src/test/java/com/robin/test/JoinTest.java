package com.robin.test;

import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.SysUserRole;
import com.robin.core.base.util.Const;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.JoinCondition;

import java.util.ArrayList;
import java.util.List;


public class JoinTest {

    public static void main(String[] args) {
        JoinCondition.Builder builder = new JoinCondition.Builder();
        builder.alias(SysUser.class, "a").alias(SysUserRole.class, "c")
                .alias(SysRole.class, "b").alias(SysResource.class, "d").alias(SysResourceRole.class, "e");

        builder.select(SysResource::getOrgId, SysResource::getName, SysResource::getName, SysResource::getCode)
                .selectAs(SysUserRole::getUserId, "oUserId").selectAs(SysRole::getRoleName, "roleName");
        builder.join(SysRole::getId, SysUserRole::getRoleId, Const.JOINTYPE.INNER).join(SysUser::getId, SysUserRole::getUserId, Const.JOINTYPE.INNER)
                .join(SysResource::getId, SysResourceRole::getResId, Const.JOINTYPE.INNER).join(SysRole::getId, SysResourceRole::getRoleId, Const.JOINTYPE.INNER);

        FilterCondition condition = new FilterCondition(SysResource::getStatus, Const.OPERATOR.EQ, 1);
        builder.withCondition(condition);
        JoinCondition joinCondition = builder.build();
        List<Object> params = new ArrayList<>();
        System.out.println(joinCondition.toSql(params));

    }
}
