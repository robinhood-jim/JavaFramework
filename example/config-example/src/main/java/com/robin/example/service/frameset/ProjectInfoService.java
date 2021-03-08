package com.robin.example.service.frameset;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.example.model.frameset.ProjectInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: ProjectInfoService </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-03-08</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
@Service
public class ProjectInfoService extends BaseAnnotationJdbcService<ProjectInfo,Long> {
    public Map<String,String> getTemplateNameByProject(ProjectInfo info){
        Map<String, String> map=new HashMap<String, String>();
        String webpagePrefix="";
        if("1".equals(info.getWebFrameId().toString())){
            webpagePrefix="dhtmlx";
        }else if("2".equals(info.getWebFrameId().toString())){
            webpagePrefix="easyui";
        }else if("3".equals(info.getWebFrameId().toString())){
            webpagePrefix="extjs";
        }
        map.put("webFramePrefix", webpagePrefix);
        String modelconfigPrefix="";
        if("1".equals(info.getPresistType())){
            modelconfigPrefix="hibernate";
        }else if("2".equals(info.getPresistType())){
            modelconfigPrefix="jpa";
        }else if("3".equals(info.getPresistType())){
            modelconfigPrefix="custom";
        }
        boolean usemvc=info.getUseMvc()!=null && "1".equals(info.getUseMvc());
        if(usemvc){
            map.put("actionType", "mvc");
        }else {
            map.put("actionType", "struts2");
        }

        map.put("modelFramePrefix", modelconfigPrefix);
        map.put("daobasepath", "src/dao");
        map.put("servicebasepath", "src/service");
        map.put("webbasepath", "src/web");
        map.put("resourcebasepath", "src/resources");
        return map;
    }
}
