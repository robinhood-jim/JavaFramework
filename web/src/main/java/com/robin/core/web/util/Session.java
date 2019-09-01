package com.robin.core.web.util;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Session implements Serializable {
    private Long userId;
    private String userCode;
    private String userName;
    private String accountName;
    private String address;
    private String zipCode;
    private String telephone;
    private String moblie;
    private String email;
    private String description;
    private Long orgId;
    private String orgCode;
    private String orgName;
    private String orgShortName;
    private String orgNumber;
    private Long deptId;
    private String deptCode;
    private String deptName;
    private String deptShortName;
    private String deptNumber;
    private Date loginTime;
    private String cityCode;
    private String accountType;
    private Map<Integer, String> roles = new HashMap();
    private Map<Long, String> responsiblitys = new HashMap();
    private Map<Integer, String> modules = new HashMap();
    private Map<String, List<Map<String, Object>>> privileges = new HashMap();
    public boolean hasRole(String key)
    {
        if (key != null) {
            return this.roles.containsKey(Integer.valueOf(key));
        }
        return false;
    }

    public boolean hasModule(String key)
    {
        if (key != null) {
            return this.modules.containsKey(Integer.valueOf(key));
        }
        return false;
    }

    public boolean hasPrivilege(String key)
    {
        return this.privileges.containsKey(key);
    }

}
