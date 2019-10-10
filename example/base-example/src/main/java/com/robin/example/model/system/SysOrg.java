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
package com.robin.example.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(table="t_sys_org_info", schema="frameset")
@Data
public class SysOrg extends BaseObject
{
    private static final long serialVersionUID = 1L;
    @MappingField(increment=true, primary=true)
    private Long id;
    @MappingField(field="tree_level")
    private Integer treeLevel;
    @MappingField(field="org_status")
    private String orgStatus;
    @MappingField(field="up_org_id")
    private Integer upOrgId;
    @MappingField(field="org_code")
    private String orgCode;
    @MappingField(field="org_name")
    private String orgName;
    @MappingField(field="tree_code")
    private String treeCode;
    @MappingField(field="org_abbr")
    private String orgAbbr;
    @MappingField(field="remark")
    private String remark;
    @MappingField(field="order_no")
    private String orderNo;
    @MappingField(field="org_type")
    private String orgType;
    public static String STATUS_ACTIVE = "1";
    public static String STATUS_INACTIVE = "0";
}
