package com.robin.example.service.frameset;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.example.model.frameset.ProjectInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Component(value="projectInfoService")
@Scope(value="singleton")
public class ProjectInfoService extends BaseAnnotationJdbcService<ProjectInfo, Long>  implements IBaseAnnotationJdbcService<ProjectInfo, Long> {

	public Map<String,String> getTemplateNameByProject(ProjectInfo info){
		Map<String, String> map=new HashMap<String, String>();
		String webpagePrefix="";
		if(info.getWebFrameId().toString().equals("1")){
			webpagePrefix="dhtmlx";
		}else if(info.getWebFrameId().toString().equals("2")){
			webpagePrefix="easyui";
		}else if(info.getWebFrameId().toString().equals("3")){
			webpagePrefix="extjs";
		}
		map.put("webFramePrefix", webpagePrefix);
		String modelconfigPrefix="";
		if(info.getPresistType().equals("1")){
			modelconfigPrefix="hibernate";
		}else if(info.getPresistType().equals("2")){
			modelconfigPrefix="jpa";
		}else if(info.getPresistType().equals("3")){
			modelconfigPrefix="custom";
		}
		boolean usemvc=info.getUseMvc()!=null && info.getUseMvc().equals("1");
		if(usemvc){
			map.put("actionType", "mvc");
		}else
			map.put("actionType", "struts2");
		
		map.put("modelFramePrefix", modelconfigPrefix);
		map.put("daobasepath", "src/dao");
		map.put("servicebasepath", "src/service");
		map.put("webbasepath", "src/web");
		map.put("resourcebasepath", "src/resources");
		return map;
	}

}
