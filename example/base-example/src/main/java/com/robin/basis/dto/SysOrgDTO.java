package com.robin.basis.dto;

import com.robin.basis.model.system.SysOrg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysOrgDTO {
    private Long id;
    private String orgName;
    private Long upOrgId;
    private String treeCode;
    private String orderNo;
    private List<SysOrgDTO> children=new ArrayList<>();
    public static SysOrgDTO fromVO(SysOrg org){
        SysOrgDTO dto=new SysOrgDTO();
        BeanUtils.copyProperties(org,dto);
        return dto;
    }
}
