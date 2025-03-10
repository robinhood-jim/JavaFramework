package com.robin.basis.dto;

import com.robin.basis.model.system.SysResource;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

@Data
public class SysResourceDTO implements Serializable {
    private Long id;
    private Long pid;
    private String resName;
    private String resCode;
    private String resType;
    private String icon;
    private String routerPath;
    private Integer seqNo;
    private String componentPath;
    private String componentName;
    private String assignType;
    private String leafTag;
    private String url;
    private String permission;
    public static SysResourceDTO fromVO(SysResource vo){
        SysResourceDTO dto=new SysResourceDTO();
        BeanUtils.copyProperties(vo,dto);
        if(ObjectUtils.isEmpty(dto.getAssignType())){
            dto.setAssignType(Const.RESOURCE_ASSIGN_DEFAULT);
        }
        return dto;
    }
}
