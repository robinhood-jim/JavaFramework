package com.robin.core.test.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
@TableName(value = "t_sys_user_info",schema = "test")
public class SysUserMybatis extends BaseObject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer orgId;

    private String accountType;

    private String userStatus;

    private String userAccount;

    private String userName;

    private String remark;

    private Integer orderNo;
    private String userPassword;
}
