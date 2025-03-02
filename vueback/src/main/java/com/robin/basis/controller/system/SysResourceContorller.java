package com.robin.basis.controller.system;

import com.robin.basis.mapper.SysResourceMapper;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
public class SysResourceContorller extends AbstractMyBatisController<ISysResourceService, SysResourceMapper,SysResource,Long> {

	@Autowired
	private ISysRoleService sysRoleService;
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
	public Map<String, Object> saveMenu(@RequestBody Map<String,Object> reqMap){
		return doSave(reqMap);
	}

	@PutMapping
	public Map<String, Object> updateSysResource(@RequestBody Map<String,Object> reqMap){
		Long id = Long.valueOf(reqMap.get("id").toString());
		try{
			return doUpdate(reqMap,id);
		}catch(Exception ex){
			return wrapFailedMsg(ex);
		}
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


}
