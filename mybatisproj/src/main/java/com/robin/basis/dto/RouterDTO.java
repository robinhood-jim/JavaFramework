package com.robin.basis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.robin.basis.model.system.SysResource;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

@Data
public class RouterDTO {
    private Long id;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String path;
    private Integer seqNo;
    private Long pid;
    private boolean hidden=false;
    private boolean iframe=false;
    private String component="Layout";
    private String assignType;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<RouterDTO> children;
    public static RouterDTO fromVO(SysResource source){
        RouterDTO dto=new RouterDTO();
        BeanUtils.copyProperties(source,dto);
        dto.setAssignType(Const.RESOURCE_ASSIGN_ACCESS);
        dto.getMeta().setTitle(source.getResName());
        dto.getMeta().setIcon(source.getIcon());
        dto.setPath(source.getRouterPath());
        dto.setName(source.getComponentName());
        if(Const.VALID.equals(source.getLeafTag().toString())){
            dto.setComponent(source.getUrl());
        }
        return dto;
    }
    public static RouterDTO fromMap(Map<String,Object> map){
        RouterDTO dto=new RouterDTO();
        try{
            ConvertUtil.mapToObject(dto,map);
            dto.setAssignType(map.get("assignType").toString());
            dto.getMeta().setTitle(map.get("name").toString());
            dto.setPath(map.get("routerPath").toString());
            if(!ObjectUtils.isEmpty(map.get("componentName"))) {
                dto.setName(map.get("componentName").toString());
            }
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
    public static RouterDTO fromDTO(SysResourceDTO dto){
        RouterDTO routerDTO=new RouterDTO();
        BeanUtils.copyProperties(dto,routerDTO);

        routerDTO.getMeta().setTitle(dto.getResName());
        routerDTO.setPath(dto.getRouterPath());
        if(!ObjectUtils.isEmpty(dto.getComponentName())) {
            routerDTO.setName(dto.getComponentName());
        }
        if(!ObjectUtils.isEmpty(dto.getId())) {
            routerDTO.getMeta().setIcon(dto.getIcon());
        }
        if(!Const.VALID.equals(dto.getLeafTag())){
            routerDTO.setComponent(dto.getUrl());
        }
        return routerDTO;
    }
    private Meta meta=new Meta();
    @Data
    public static class Meta {
        private String title;

        private String icon;
    }
}
