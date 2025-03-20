package com.robin.basis.controller.system;

import com.robin.basis.dto.query.SysResourceQueryDTO;
import com.robin.basis.mapper.SysResourceMapper;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.basis.utils.SecurityUtils;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
	@GetMapping
	@PreAuthorize("@checker.isAdmin()")
	public Map<String,Object> list(SysResourceQueryDTO dto) {
		return wrapObject(service.search(dto));
	}

	@PostMapping("/save")
	@PreAuthorize("@checker.isAdmin()")
	public Map<String, Object> saveMenu(@RequestBody Map<String,Object> reqMap){
		return doSave(reqMap,null);
	}

	@PutMapping
	@PreAuthorize("@checker.isAdmin()")
	public Map<String, Object> updateSysResource(@RequestBody Map<String,Object> reqMap){
		Long id = Long.valueOf(reqMap.get("id").toString());
		try{
			return doUpdate(reqMap,id);
		}catch(Exception ex){
			return wrapFailedMsg(ex);
		}
	}
	@GetMapping("/getByUser/{userId}")
	@PreAuthorize("@checker.isAdmin()")
	public Map<String,Object> getUserPermission(@PathVariable Long userId){
		List<Long> ids=service.getPermissionIdByUser(userId, SecurityUtils.getLoginUser().getTenantId());
		Map<String,Object> map=new HashMap<>();
		map.put("ids",ids);
		return wrapObject(map);
	}




}
