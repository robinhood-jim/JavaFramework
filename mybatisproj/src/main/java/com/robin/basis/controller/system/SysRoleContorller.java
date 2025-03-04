package com.robin.basis.controller.system;

import com.robin.basis.dto.SysRoleDTO;
import com.robin.basis.dto.query.SysRoleQueryDTO;
import com.robin.basis.mapper.SysRoleMapper;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/role")
public class SysRoleContorller extends AbstractMyBatisController<ISysRoleService, SysRoleMapper,SysRole,Long> {

	@GetMapping
	public Map<String,Object> listRole(@RequestBody SysRoleQueryDTO dto) {
		return service.search(dto);
	}

	@PostMapping
	public Map<String, Object> saveRole(@RequestBody SysRoleDTO dto){
		service.saveRole(dto);
		return wrapSuccess("OK");
	}
	@PutMapping
	public Map<String, Object> updateRole(@RequestBody SysRoleDTO dto){
		service.updateRole(dto);
		return wrapSuccess("OK");
	}
	@GetMapping("/all")
	public Map<String,Object> showAllRole(){
		try{
			List<SysRole> roles=service.queryByField(SysRole::getStatus, Const.OPERATOR.EQ,Const.VALID);
			return wrapObject(roles.stream().map(SysRoleDTO::fromVO).collect(Collectors.toList()));
		}catch (Exception ex){
			return wrapError(ex);
		}
	}

	@DeleteMapping
	public Map<String,Object> deleteRole(@RequestBody List<Long> ids){
		Map<String,Object> retMap=new HashMap<>();
		try{
			service.deleteRoles(ids);
			constructRetMap(retMap);
		}catch(Exception ex){
			wrapFailed(retMap,ex);
		}
		return retMap;
	}
}
