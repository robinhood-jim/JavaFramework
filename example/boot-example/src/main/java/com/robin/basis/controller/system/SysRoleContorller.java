package com.robin.basis.controller.system;

import com.google.gson.Gson;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.service.system.SysResourceService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.service.system.SysRoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/system/role")
public class SysRoleContorller extends AbstractCrudDhtmlxController<SysRole,Long, SysRoleService> {
	@Resource
	private SysResourceService sysResourceService;

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
			ConvertUtil.mapToObject(map, tmpuser);
			ConvertUtil.convertToModelForUpdate(tmpuser, user);
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

	@GetMapping("/showright/{roleId}")
	public String userRight(HttpServletRequest request, @PathVariable Long roleId) {
		request.setAttribute("roleId", roleId);
		return "role/show_right";
	}
	@GetMapping("/listright/{roleId}")
	@ResponseBody
	public Map<String,Object> lisright(HttpServletRequest request,@PathVariable Long roleId){
		List<Map<String, Object>> retList = new ArrayList<>();
		List<Long> resIdList = new ArrayList<>();
		try {
			PageQuery query=new PageQuery();
			query.setPageSize(0);

			query.setSelectParamId("GET_SYSRESOURCEBYROLE");
			//query.setSelectParamId("GET_ORGRESOURCEBYRESP");

			query.addQueryParameter(new Object[]{roleId});
			service.queryBySelectId(query);
			List<Map<String, Object>> list = query.getRecordSet();

			for (Map<String, Object> map : list) {
				resIdList.add(Long.valueOf(map.get("id").toString()));
			}
			List<SysResource> resList = sysResourceService.queryByField("status", Const.OPERATOR.EQ, "1");

			filterMenu(resList,retList,resIdList);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Map<String, Object> retMaps = new HashMap<String, Object>();
		retMaps.put("id", "0");
		retMaps.put("text", "菜单");
		retMaps.put("item", retList);
		return retMaps;
	}
	private void filterMenu(List<SysResource> resList,List<Map<String, Object>> retList,List<Long> resIdList){
		List<Long> addList = new ArrayList<Long>();
		List<Long> delList = new ArrayList<Long>();
		Map<String, Object> rmap = new HashMap<String, Object>();

		for (SysResource res : resList) {
			String pid = res.getPid().toString();
			if ("0".equals(pid)) {
				Map<String, Object> tmap = new HashMap<String, Object>();
				tmap.put("id", res.getId());
				tmap.put("text", res.getName());
				rmap.put(res.getId().toString(), tmap);
				retList.add(tmap);
			} else {
				if (rmap.containsKey(pid)) {
					Map<String, Object> tmpmap = (Map<String, Object>) rmap.get(pid);
					Map<String, Object> t2map = new HashMap<String, Object>();
					t2map.put("id", res.getId());
					t2map.put("text", res.getName());
					if (resIdList.contains(res.getId())) {
						if (delList.contains(res.getId())) {
							t2map.put("style", "font-weight:bold;text-decoration:underline;color:#ee1010");
						} else {
							t2map.put("checked", "1");
							t2map.put("style", "font-weight:bold;text-decoration:underline");
						}
					} else if (addList.contains(res.getId())) {
						t2map.put("checked", "1");
						t2map.put("style", "font-weight:bold;color:#1010ee");
					}
					if (!tmpmap.containsKey("item")) {
						List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
						list1.add(t2map);
						tmpmap.put("item", list1);
					} else {
						List<Map<String, Object>> list1 = (List<Map<String, Object>>) tmpmap.get("item");
						list1.add(t2map);
					}
				}
			}
		}
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
