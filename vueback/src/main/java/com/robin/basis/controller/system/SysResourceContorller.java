package com.robin.basis.controller.system;

import com.robin.basis.dto.query.SysResourceQueryDTO;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
public class SysResourceContorller extends AbstractMyBatisController<ISysResourceService, SysResourceMapper,SysResource,Long> {

	@Autowired
	private ISysRoleService sysRoleService;
	@GetMapping
	public Map<String,Object> list(SysResourceQueryDTO dto) {
		return wrapObject(service.search(dto));
	}

	@PostMapping("/save")
	public Map<String, Object> saveMenu(@RequestBody Map<String,Object> reqMap){

		return doSave(reqMap,null);
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
