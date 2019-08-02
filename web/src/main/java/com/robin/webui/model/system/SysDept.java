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
package com.robin.webui.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table="t_sys_dept_info", schema="frameset")
@Data
public class SysDept extends BaseObject
{
    private static final long serialVersionUID = 1L;
    @MappingField(increment="1", primary="1")
    private Long id;
    @MappingField(field="dept_name")
    private String deptName;
    @MappingField(field="dept_abbr")
    private String deptAbbr;
    @MappingField(field="tree_level")
    private Integer treeLevel;
    @MappingField(field="dept_code")
    private String deptCode;
    @MappingField(field="dept_status")
    private String deptStatus;
    @MappingField(field="up_dept_id")
    private Integer upDeptId;
    @MappingField(field="org_id")
    private Integer orgId;
    @MappingField(field="leader_user_id")
    private Integer leaderUserId;
    @MappingField(field="tree_code")
    private String treeCode;
    @MappingField(field="remark")
    private String remark;
    @MappingField(field="order_no")
    private String orderNo;
}
