package com.robin.basis.dto;

import com.robin.basis.model.system.SysOrg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysOrgDTO {
    private Long id;
    private String label;
    private Long upOrgId;
    private List<SysOrgDTO> children=new ArrayList<>();
    public static SysOrgDTO fromVO(SysOrg org){
        SysOrgDTO dto=new SysOrgDTO();
        dto.setId(org.getId());
        dto.setUpOrgId(org.getUpOrgId());
        dto.setLabel(org.getOrgName());
        return dto;
    }
}
