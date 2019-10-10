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


@MappingEntity(table="t_sys_user_role_r", schema="frameset")
@Data
public class SysUserRole extends BaseObject
{
    private static final long serialVersionUID = 1L;
    @MappingField(increment=true, primary=true)
    private Long id;
    @MappingField(field="role_id")
    private Integer roleId;
    @MappingField(field="status")
    private String status;
    @MappingField(field="user_id")
    private Long userId;
    @MappingField(field="oper")
    private String oper;

    public static final String REF_CLASS = "SysUserRole";
    public static final String PROP_ROLE_ID = "roleId";
    public static final String PROP_STATUS = "status";
    public static final String PROP_USER_ID = "userId";
    public static final String PROP_OPER = "oper";
    public static final String PROP_ID = "id";
    public static final String REF_TABLE = "t_sys_user_role_r";
    public static final String COL_ROLE_ID = "ROLE_ID";
    public static final String COL_STATUS = "STATUS";
    public static final String COL_USER_ID = "USER_ID";
    public static final String COL_OPER = "OPER";
    public static final String COL_ID = "ID";
}
