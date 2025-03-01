package com.robin.basis.controller.system;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.service.system.SysResourceService;
import com.robin.basis.service.system.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
public class SysResourceContorller extends AbstractCrudDhtmlxController<SysResource,Long, SysResourceService> {

	@Autowired
	private SysRoleService sysRoleService;
	@PostMapping("/list")
	public Map<String,Object> list(HttpServletRequest request,
			HttpServletResponse response) {
		List<Map<String,Object>> list=service.queryBySql("select id,res_name as name,url,pid from t_sys_resource_info where RES_TYPE='1' ORDER BY RES_CODE,PID,SEQ_NO");
		List<Map<String,Object>> retList=new ArrayList<>();
		Map<String,Object> rmap=new HashMap<>();
		for (Map<String,Object> map:list) {
			String pid=map.get("pid").toString();
			if("0".equals(pid)){
				Map<String,Object> tmap=new HashMap<String,Object>();
				tmap.put("id", map.get("id"));
				tmap.put("text", map.get("name"));
				rmap.put(map.get("id").toString(), tmap);
				retList.add(tmap);
			}else{
				if(rmap.containsKey(pid)){
					Map<String, Object> tmpmap=(Map<String, Object>) rmap.get(pid);
					Map<String, Object> t2map=new HashMap<String,Object>();
					t2map.put("id", map.get("id"));
					t2map.put("text", map.get("name"));
					List<Map<String,String>> userdataList=new ArrayList<Map<String,String>>();
					Map<String,String> usermap=new HashMap<String,String>();
					usermap.put("name", "url");
					usermap.put("value", map.get("url").toString());
					userdataList.add(usermap);
					t2map.put("userdata", userdataList);
					
					if(!tmpmap.containsKey("item")){
						List<Map<String, Object>> list1=new ArrayList<>();
						list1.add(t2map);
						tmpmap.put("item", list1);
					}else{
						List<Map<String, Object>> list1=(List<Map<String, Object>>) tmpmap.get("item");
						list1.add(t2map);
					}
				}
			}
		}
		Map<String, Object> retMaps=new HashMap<>();
		retMaps.put("id", "0");
		retMaps.put("text", "菜单");
		retMaps.put("item", retList);
		return retMaps;
	}

	@PostMapping("/save")
	public Map<String, Object> saveMenu(HttpServletRequest request,
			HttpServletResponse response){
		Map<String, Object> retmap=new HashMap<>();
		try{
			Map<String,Object> map=wrapRequest(request);
			SysResource resource=new SysResource();
			ConvertUtil.convertToModel(resource, map);
			resource.setType("1");
			Long id=service.saveEntity(resource);
			retmap.put("id", String.valueOf(id));
			retmap.put("success", "true");
			retmap.put("menu", resource);
		}catch(Exception ex){
			ex.printStackTrace();
			retmap.put("success", "false");
			retmap.put("message", ex.getMessage());
		}
		return retmap;
	}
	

	@GetMapping("/edit/{id}")
	public SysResource queryUser(HttpServletRequest request,@PathVariable String id){
		SysResource resource=service.getEntity(Long.valueOf(id));
		return resource;
	}
	@PostMapping("/update")
	public Map<String, Object> updateSysResource(HttpServletRequest request,
			HttpServletResponse response){
		Map<String, Object>  retmap=new HashMap<>();
		try{
			Map<String,Object> map=wrapRequest(request);
			Long id=Long.valueOf(request.getParameter("id"));
			SysResource user=service.getEntity(id);
			SysResource tmpuser=new SysResource();
			ConvertUtil.convertToModel(tmpuser, map);
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
	@PostMapping("/assignrole")
	public Map<String, Object> assignRole(HttpServletRequest request,HttpServletResponse response){
		String[] ids=request.getParameter("selRoleIds").split(",");
		Map<String, Object> retmap=new HashMap<>();
		try{
			sysRoleService.saveRoleRigth(ids, request.getParameter("resId"));
		
			retmap.put("success", "true");
		}catch(ServiceException ex){
			ex.printStackTrace();
			retmap.put("success", "false");
		}
		return retmap;
	}
	@GetMapping("/listright/{userId}")
	public Map<String,Object> userRights(@PathVariable Long userId){
		return service.getUserRights(userId);
	}

	@Override
	protected String wrapQuery(HttpServletRequest request, PageQuery query) {
		return null;
	}
}
