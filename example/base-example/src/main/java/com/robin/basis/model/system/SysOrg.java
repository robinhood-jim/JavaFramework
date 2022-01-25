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


@MappingEntity(table="t_sys_org_info", schema="frameset")
public class SysOrg extends BaseObject
{
    @MappingField(increment=true, primary=true)
    private Long id;
    @MappingField(field="tree_level")
    private Integer treeLevel;
    @MappingField(field="org_status")
    private String orgStatus;
    @MappingField(field="up_org_id")
    private Long upOrgId;
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
    @MappingField
    private Long tenantId;
    public static String STATUS_ACTIVE = "1";
    public static String STATUS_INACTIVE = "0";
    public static String ORGTYPE_CUSTTOP="1";
    public static String TOP_TREECODE="0001";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(Integer treeLevel) {
        this.treeLevel = treeLevel;
    }

    public String getOrgStatus() {
        return orgStatus;
    }

    public void setOrgStatus(String orgStatus) {
        this.orgStatus = orgStatus;
    }

    public Long getUpOrgId() {
        return upOrgId;
    }

    public void setUpOrgId(Long upOrgId) {
        this.upOrgId = upOrgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    public String getOrgAbbr() {
        return orgAbbr;
    }

    public void setOrgAbbr(String orgAbbr) {
        this.orgAbbr = orgAbbr;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
