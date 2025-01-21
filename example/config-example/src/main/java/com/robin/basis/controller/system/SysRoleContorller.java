package com.robin.basis.controller.system;

import com.google.gson.Gson;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.service.system.SysRoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
@Controller
@RequestMapping("/system/role")
public class SysRoleContorller extends AbstractCrudDhtmlxController<SysRole,Long, SysRoleService> {

	@PostMapping("/list")
	@ResponseBody
	public Map<String,Object> listRole(HttpServletRequest request,
			HttpServletResponse response) {
		PageQuery query=wrapPageQuery(request);
		query.setSelectParamId("GET_SYSROLE_PAGE");
		query.getParameters().put("queryString", wrapQuery(request,query));
		service.queryBySelectId(query);
		setCode("ROLETYPE,VALIDTAG");
		filterListByCodeSet(query,"type","ROLETYPE",null);
		filterListByCodeSet(query,"status","VALIDTAG",null);
		return wrapDhtmlxGridOutput(query);
	}
	@GetMapping("/show")
	public String showRole(ModelMap model,HttpServletRequest request,
			HttpServletResponse response){
		
		return "role/role_list";
	}
	@GetMapping("/edit/{id}")
	@ResponseBody
	public SysRole queryRole(HttpServletRequest request,
							 @PathVariable String id){
		SysRole user=service.getEntity(Long.valueOf(id));
		return user;
	}
	@PostMapping("/save")
	@ResponseBody
	public String saveRole(HttpServletRequest request,
			HttpServletResponse response){
		Map<String, String>  retmap=new HashMap<>();
		try{
			Map<String,Object> map=wrapRequest(request);
			SysRole user=new SysRole();
			ConvertUtil.convertToModel(user, map);
			Long id=service.saveEntity(user);
			retmap.put("id", String.valueOf(id));
			retmap.put("success", "true");
		}catch(Exception ex){
			ex.printStackTrace();
			retmap.put("success", "false");
			retmap.put("message", ex.getMessage());
		}
		Gson gson=new Gson();
		return gson.toJson(retmap);
	}
	@PostMapping("/update")
	@ResponseBody
	public Map<String, Object> updateRole(HttpServletRequest request,
			HttpServletResponse response){
		Map<String, Object>  retmap=new HashMap<String,Object>();
		try{
			Map<String,Object> map=wrapRequest(request);
			Long id=Long.valueOf(request.getParameter("id"));
			SysRole user=service.getEntity(id);
			SysRole tmpuser=new SysRole();
			ConvertUtil.mapToObject(tmpuser, map);
			ConvertUtil.convertToModelForUpdate(user, tmpuser);
			service.updateEntity(user);
			retmap.put("id", String.valueOf(id));
			retmap.put("success", "true");
		}catch(Exception ex){
			ex.printStackTrace();
			retmap.put("success", "false");
			retmap.put("message", ex.getMessage());
		}
		
		return retmap;
	}
	@GetMapping("/delete")
	@ResponseBody
	public Map<String,String> deleteRole(HttpServletRequest request,
			HttpServletResponse response){
		Map<String, String>  retmap=new HashMap<String,String>();
		String[] ids=request.getParameter("ids").split(",");
		Long[] idAdd=new Long[ids.length];
		for (int i = 0; i < idAdd.length; i++) {
			idAdd[i]=Long.valueOf(ids[i]);
		}
		try{
			int ret=service.deleteEntity(idAdd);
			retmap.put("success", "true");
		}catch(Exception ex){
			ex.printStackTrace();
			retmap.put("success", "false");
			retmap.put("message", ex.getMessage());
		}
		return retmap;
	}
	@Override
	public String wrapQuery(HttpServletRequest request,PageQuery query){
		StringBuilder builder=new StringBuilder();
		if( request.getParameter("roleName")!=null && !"".equals(request.getParameter("roleName"))){
			builder.append(" and name like '%"+request.getParameter("roleName")+"%'");
		}
		return builder.toString();
	}
}
