/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
