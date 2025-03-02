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
package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(value ="t_sys_role_info")
@Data
@TableName("t_sys_role_info")
public class SysRole extends BaseObject
{
    @MappingField(primary=true, increment=true)
    @TableId(type = IdType.AUTO)
    private Long id;
    @MappingField(value ="role_name")
    private String roleName;
    @MappingField(value ="role_type")
    private String roleType;
    @MappingField
    private String status;
    @MappingField(value ="role_code")
    private String roleCode;
    @MappingField(value ="role_desc")
    private String roleDesc;
}
