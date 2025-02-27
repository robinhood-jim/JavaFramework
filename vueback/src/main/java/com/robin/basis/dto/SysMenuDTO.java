package com.robin.basis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.robin.basis.model.system.SysResource;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.web.util.WebConstant;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SysMenuDTO {
    private Long id;
    private String name;
    private String path;
    private Integer seqNo;
    private Long pid;
    private boolean hidden=false;
    private boolean iframe=false;
    private String component="Layout";
    private String assignType;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SysMenuDTO> children;
    public static SysMenuDTO fromVO(SysResource source){
        SysMenuDTO dto=new SysMenuDTO();
        BeanUtils.copyProperties(source,dto);
        dto.setAssignType(Const.RESOURCE_ASSIGN_ACCESS);
        dto.getMeta().setTitle(source.getName());
        dto.getMeta().setIcon(source.getIcon());
        dto.setPath(source.getUrl());
        if(Const.VALID.equals(source.getLeafTag().toString())){
            dto.setComponent(source.getUrl());
        }
        return dto;
    }
    public static SysMenuDTO fromMap(Map<String,Object> map){
        SysMenuDTO dto=new SysMenuDTO();
        try{
            ConvertUtil.mapToObject(dto,map);
            dto.setAssignType(map.get("assignType").toString());
            dto.getMeta().setTitle(map.get("name").toString());
            dto.setPath(map.get("url").toString());
            if(!ObjectUtils.isEmpty(map.get("icon"))) {
                dto.getMeta().setIcon(map.get("icon").toString());
            }
            if(!Const.VALID.equals(map.get("leafTag").toString())){
                dto.setComponent(map.get("url").toString());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return dto;
    }
    private Meta meta=new Meta();
    @Data
    public static class Meta {
        private String title;

        private String icon;
    }
}
