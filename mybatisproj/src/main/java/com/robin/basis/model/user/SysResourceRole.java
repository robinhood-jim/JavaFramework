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


@MappingEntity(value = "t_sys_resource_role_r")
@TableName("t_sys_resource_role_r")
@Data
public class SysResourceRole extends BaseObject {

    // primary key
    @MappingField(primary = true, increment = true)
    @TableId(type = IdType.AUTO)
    private Long id;   //

    // fields
    @MappingField(value = "role_id")
    private Integer roleId;
    @MappingField(value = "status")
    private String status;
    @MappingField(value = "res_id")
    private Integer resId;


    public static String REF_CLASS = "SysResourceRole";
    public static String PROP_ROLE_ID = "roleId";
    public static String PROP_STATUS = "status";
    public static String PROP_RES_ID = "resId";
    public static String PROP_ID = "id";

    public static String REF_TABLE = "t_sys_resource_role_r";
    public static String COL_ROLE_ID = "ROLE_ID";
    public static String COL_STATUS = "STATUS";
    public static String COL_RES_ID = "RES_ID";
    public static String COL_ID = "ID";

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof SysResourceRole)) {
            return false;
        } else {
            SysResourceRole o = (SysResourceRole) obj;
            if (null == this.getId() || null == o.getId()) {
                return false;
            } else {
                return (this.getId().equals(o.getId()));
            }
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[SysResourceRole:");
        buffer.append(" id:").append(id);
        buffer.append(" roleId:").append(dealNull(roleId));
        buffer.append(" status:").append(dealNull(status));
        buffer.append(" resId:").append(dealNull(resId));
        buffer.append("]");
        return buffer.toString();
    }

    public String toJson() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        buffer.append("\"id\":\"").append(id).append("\"");
        buffer.append(",\"roleId\":\"").append(dealNull(roleId)).append("\"");
        buffer.append(",\"status\":\"").append(dealNull(status)).append("\"");
        buffer.append(",\"resId\":\"").append(dealNull(resId)).append("\"");
        buffer.append("}");
        return buffer.toString();
    }

    private String dealNull(Object str) {
        if (str == null) {
            return "";
        } else {
            return str.toString();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getResId() {
        return resId;
    }

    public void setResId(Integer resId) {
        this.resId = resId;
    }
}
