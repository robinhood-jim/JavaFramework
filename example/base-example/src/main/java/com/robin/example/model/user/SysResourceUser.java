package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table = "t_sys_resource_user_r", schema = "frameset")
@Data
public class SysResourceUser extends BaseObject {
	private static final long serialVersionUID = 1L;

	// primary key
	@MappingField(primary = "1", increment = "1")
	private Long id; //

	// fields
	@MappingField
	private String status;
	@MappingField(field = "res_id")
	private Integer resId;
	@MappingField(field = "user_id")
	private Integer userId;
	@MappingField(field="assign_type")
	private String assignType;

	public static String ASSIGN_ADD = "1";
	public static String ASSIGN_DEL = "2";


	public static String REF_CLASS = "SysResourceUser";
	public static String PROP_STATUS = "status";
	public static String PROP_RES_ID = "resId";
	public static String PROP_USER_ID = "userId";
	public static String PROP_ID = "id";

	public static String REF_TABLE = "t_sys_resource_user_r";
	public static String COL_STATUS = "STATUS";
	public static String COL_RES_ID = "RES_ID";
	public static String COL_USER_ID = "USER_ID";
	public static String COL_ID = "ID";

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SysResourceUser))
			return false;
		else {
			SysResourceUser o = (SysResourceUser) obj;
			if (null == this.getId() || null == o.getId())
				return false;
			else
				return (this.getId().equals(o.getId()));
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[SysResourceUser:");
		buffer.append(" id:").append(id);
		buffer.append(" status:").append(dealNull(status));
		buffer.append(" resId:").append(dealNull(resId));
		buffer.append(" userId:").append(dealNull(userId));
		buffer.append("]");
		return buffer.toString();
	}

	public String toJson() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"id\":\"").append(id).append("\"");
		buffer.append(",\"status\":\"").append(dealNull(status)).append("\"");
		buffer.append(",\"resId\":\"").append(dealNull(resId)).append("\"");
		buffer.append(",\"userId\":\"").append(dealNull(userId)).append("\"");
		buffer.append("}");
		return buffer.toString();
	}

	private String dealNull(Object str) {
		if (str == null)
			return "";
		else
			return str.toString();
	}
}
