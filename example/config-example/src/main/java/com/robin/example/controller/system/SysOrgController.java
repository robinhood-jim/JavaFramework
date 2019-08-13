package com.robin.example.controller.system;

import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.BaseCrudDhtmlxController;
import com.robin.example.model.system.SysOrg;
import com.robin.example.service.system.SysOrgService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/org")
public class SysOrgController extends BaseCrudDhtmlxController<SysOrg,Long,SysOrgService> {


	@RequestMapping("/listjson")
	@ResponseBody
	public Map<String,Object> getdeptJson(HttpServletRequest request,HttpServletResponse response){
		String allowNull=request.getParameter("allowNull");
		boolean insertNullVal=true;
		if(allowNull!=null && !allowNull.isEmpty() && allowNull.equalsIgnoreCase("false")){
			insertNullVal=false;
		}
		PageQuery query=new PageQuery();
		query.setSelectParamId("GET_ORGINFO");
		query.setPageSize("0");
		service.queryBySelectId(query);
		Map<String, Object> map=new HashMap<String, Object>();
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		if(insertNullVal){
			insertNullSelect(list);
		}
		list.addAll(query.getRecordSet());
		map.put("options", list);
		return map;
	}

}
