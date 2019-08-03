package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(table = "t_sys_resource_role_r", schema = "frameset")
@Data
public class SysResourceRole extends BaseObject {
    private static final long serialVersionUID = 1L;

    // primary key
    @MappingField(primary = "1", increment = "1")
    private Long id;   //

    // fields
    @MappingField(field = "role_id")
    private Integer roleId;
    @MappingField(field = "status")
    private String status;
    @MappingField(field = "res_id")
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

    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof SysResourceRole)) return false;
        else {
            SysResourceRole o = (SysResourceRole) obj;
            if (null == this.getId() || null == o.getId()) return false;
            else return (this.getId().equals(o.getId()));
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[SysResourceRole:");
        buffer.append(" id:").append(id);
        buffer.append(" roleId:").append(dealNull(roleId));
        buffer.append(" status:").append(dealNull(status));
        buffer.append(" resId:").append(dealNull(resId));
        buffer.append("]");
        return buffer.toString();
    }

    public String toJson() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"id\":\"").append(id).append("\"");
        buffer.append(",\"roleId\":\"").append(dealNull(roleId)).append("\"");
        buffer.append(",\"status\":\"").append(dealNull(status)).append("\"");
        buffer.append(",\"resId\":\"").append(dealNull(resId)).append("\"");
        buffer.append("}");
        return buffer.toString();
    }

    private String dealNull(Object str) {
        if (str == null) return "";
        else return str.toString();
    }
}
