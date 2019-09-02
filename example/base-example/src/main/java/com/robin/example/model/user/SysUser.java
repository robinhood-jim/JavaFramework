/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table = "t_sys_user_info",schema = "frameset")
@Data
public class SysUser extends BaseObject {
    private static final long serialVersionUID = 1L;
    @MappingField(primary=true, increment=true)
    private Long id;
    @MappingField(field="org_id")
    private Long orgId;
    @MappingField(field="account_type")
    private String accountType;
    @MappingField(field="user_status")
    private String userStatus;
    @MappingField(field="user_account")
    private String userAccount;
    @MappingField(field="user_name")
    private String userName;
    @MappingField(field="remark")
    private String remark;
    @MappingField(field="order_no")
    private Integer orderNo;
    @MappingField(field="user_password")
    private String userPassword;


}
