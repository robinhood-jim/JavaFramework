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
package com.robin.basis.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(value ="t_sys_org_info")
@Data
public class SysOrg extends BaseObject
{
    @MappingField(increment=true, primary=true)
    private Long id;
    @MappingField(value ="tree_level")
    private Integer treeLevel;
    @MappingField(value ="org_status")
    private String orgStatus;
    @MappingField(value ="up_org_id")
    private Long upOrgId;
    @MappingField(value ="org_code")
    private String orgCode;
    @MappingField(value ="org_name")
    private String orgName;
    @MappingField(value ="tree_code")
    private String treeCode;
    @MappingField(value ="org_abbr")
    private String orgAbbr;
    @MappingField(value ="remark")
    private String remark;
    @MappingField(value ="order_no")
    private String orderNo;
    @MappingField(value ="org_type")
    private String orgType;
    @MappingField
    private Long tenantId;
    public static String STATUS_ACTIVE = "1";
    public static String STATUS_INACTIVE = "0";
    public static String ORGTYPE_CUSTTOP="1";
    public static String TOP_TREECODE="0001";


}
