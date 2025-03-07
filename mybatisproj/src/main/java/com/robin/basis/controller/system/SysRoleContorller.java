package com.robin.basis.controller.system;

import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.dto.SysRoleDTO;
import com.robin.basis.dto.query.SysRoleQueryDTO;
import com.robin.basis.mapper.SysRoleMapper;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.service.system.ISysResourceRoleService;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/role")
public class SysRoleContorller extends AbstractMyBatisController<ISysRoleService, SysRoleMapper,SysRole,Long> {
	@Resource
	private ISysResourceRoleService sysResourceRoleService;


	@GetMapping
	@PreAuthorize("@checker.isAdmin()")
	public Map<String,Object> listRole(SysRoleQueryDTO dto) {
		return wrapObject(service.search(dto));
	}

	@PostMapping
	@PreAuthorize("@checker.isAdmin()")
	public Map<String, Object> saveRole(@RequestBody SysRoleDTO dto){
		service.saveRole(dto);
		return wrapSuccess("OK");
	}
	@PutMapping
	@PreAuthorize("@checker.isAdmin()")
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
	@GetMapping("/menu/{roleId}")
	public Map<String,Object> showRoleMenu(@PathVariable Long roleId){
		try{
			List<SysResourceDTO> premissions=sysResourceRoleService.queryResourceByRole(roleId);
			return wrapObject(premissions);
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
	@PostMapping("/{id}/permission")
	@PreAuthorize("@checker.isAdmin()")
	public Map<String,Object> savePermission(@PathVariable Long id,@RequestBody List<Long> permissions){
		sysResourceRoleService.updateUserResourceRight(id,permissions);
		return wrapSuccess("OK");
	}
}
