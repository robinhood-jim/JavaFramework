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

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

@MappingEntity(table = "t_sys_resource_user_r", schema = "frameset")
public class SysResourceUser extends BaseObject {

	// primary key
	@MappingField(primary = true, increment = true)
	private Long id; //

	// fields
	@MappingField
	private String status;
	@MappingField(field = "res_id")
	private Integer resId;
	@MappingField(field = "user_id")
	private Integer userId;
	@MappingField(field="assign_type")
	private Integer assignType;

	public static final int ASSIGN_ADD = 1;
	public static final int ASSIGN_DEL = 2;


	public static final String REF_CLASS = "SysResourceUser";
	public static final String PROP_STATUS = "status";
	public static final String PROP_RES_ID = "resId";
	public static final String PROP_USER_ID = "userId";
	public static final String PROP_ID = "id";

	public static final String REF_TABLE = "t_sys_resource_user_r";
	public static final String COL_STATUS = "STATUS";
	public static final String COL_RES_ID = "RES_ID";
	public static final String COL_USER_ID = "USER_ID";
	public static final String COL_ID = "ID";

	@Override
    public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
		if (!(obj instanceof SysResourceUser)) {
            return false;
        } else {
			SysResourceUser o = (SysResourceUser) obj;
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
		buffer.append("[SysResourceUser:");
		buffer.append(" id:").append(id);
		buffer.append(" status:").append(dealNull(status));
		buffer.append(" resId:").append(dealNull(resId));
		buffer.append(" userId:").append(dealNull(userId));
		buffer.append("]");
		return buffer.toString();
	}

	public String toJson() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("{");
		buffer.append("\"id\":\"").append(id).append("\"");
		buffer.append(",\"status\":\"").append(dealNull(status)).append("\"");
		buffer.append(",\"resId\":\"").append(dealNull(resId)).append("\"");
		buffer.append(",\"userId\":\"").append(dealNull(userId)).append("\"");
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

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getAssignType() {
		return assignType;
	}

	public void setAssignType(Integer assignType) {
		this.assignType = assignType;
	}
}
