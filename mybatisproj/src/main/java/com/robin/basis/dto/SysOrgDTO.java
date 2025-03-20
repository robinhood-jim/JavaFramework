package com.robin.basis.dto;

import com.robin.basis.model.system.SysOrg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SysOrgDTO implements Serializable {
    private Long id;
    private String pid;
    private String orgCode;
    private String orgName;
    private String treeCode;
    private String orgAbbr;
    private String remark;
    private String orderNo;
    private Long tenantId;

    public static SysOrgDTO fromVO(SysOrg org){
        SysOrgDTO dto=new SysOrgDTO();
        BeanUtils.copyProperties(org,dto);
        return dto;
    }
}
