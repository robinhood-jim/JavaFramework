package com.robin.basis.dto;

import com.robin.basis.model.system.SysOrg;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysOrgDTO {
    private Long id;
    private String label;
    private Long pid;
    private List<SysOrgDTO> children=new ArrayList<>();
    public static SysOrgDTO fromVO(SysOrg org){
        SysOrgDTO dto=new SysOrgDTO();
        dto.setId(org.getId());
        dto.setPid(org.getPid());
        dto.setLabel(org.getOrgName());
        return dto;
    }
}
