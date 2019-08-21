package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.example.model.frameset</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月13日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@MappingEntity(table = "t_user_credential")
@Data
public class SysCredential extends BaseObject {
    @MappingField(primary = "1",increment = "1")
    private Long id;
    @MappingField(required = true)
    private String code;
    @MappingField(field = "user_name")
    private String userName;
    @MappingField
    private String password;
    @MappingField
    private Integer status;
    @MappingField
    private String token;
    @MappingField(field = "ssl_path")
    private String sslConfigPath;
    //1-public  2-require authorize
    private Integer type;



}
