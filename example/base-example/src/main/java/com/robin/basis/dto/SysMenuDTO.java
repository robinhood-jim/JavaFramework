package com.robin.basis.dto;

import com.robin.basis.model.system.SysResource;
import com.robin.core.convert.util.ConvertUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SysMenuDTO {
    private Long id;
    private String type;
    private String title;
    private String path;
    private Integer seqNo;
    private Long pid;
    private Integer leafTag;
    private String assignType;
    private List<SysMenuDTO> children=new ArrayList<>();
    public static SysMenuDTO fromVO(SysResource source){
        SysMenuDTO dto=new SysMenuDTO();
        BeanUtils.copyProperties(source,dto);
        dto.setTitle(source.getName());
        dto.setPath(source.getUrl());
        return dto;
    }
    public static SysMenuDTO fromMap(Map<String,Object> map){
        SysMenuDTO dto=new SysMenuDTO();
        try{
            ConvertUtil.mapToObject(dto,map);
            dto.setAssignType(map.get("assignType").toString());
            dto.setTitle(map.get("name").toString());
            dto.setPath(map.get("url").toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return dto;
    }
}
