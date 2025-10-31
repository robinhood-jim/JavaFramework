package com.robin.basis.controller.system;

import com.robin.basis.dto.SysRoleDTO;
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

	@PostMapping("/list")
	public Map<String,Object> listRole(HttpServletRequest request,
			HttpServletResponse response) {
		PageQuery query=wrapPageQuery(request);
		query.setSelectParamId("GET_SYSROLE_PAGE");
		query.getParameters().put("queryString", wrapQuery(request,query));
		service.queryBySelectId(query);
		Map<String,Object> retMap=new HashMap<>();
		retMap.put("rows",query.getRecordSet());
		retMap.put("total",query.getTotal());
		return retMap;
	}

	@PostMapping("/save")
	public Map<String, Object> saveRole(HttpServletRequest request,
										HttpServletResponse response){
		Map<String, Object>  retmap=new HashMap<>();
		try{
			Map<String,Object> map=wrapRequest(request);
			SysRole user=new SysRole();
			ConvertUtil.convertToModel(map, user);
			service.save(user);
			wrapSuccessMap(retmap,"OK");
		}catch(Exception ex){
			wrapFailed(retmap,ex);
		}
		return retmap;
	}
	@PutMapping("/update")
	public Map<String, Object> updateRole(@RequestBody Map<String,Object> reqMap){
		Long id = Long.valueOf(reqMap.get("id").toString());
		//check userAccount unique
		return doUpdate(reqMap, id);
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
			service.deleteByIds(ids);
			constructRetMap(retMap);
		}catch(Exception ex){
			wrapFailed(retMap,ex);
		}
		return retMap;
	}

	public String wrapQuery(HttpServletRequest request,PageQuery query){
		StringBuilder builder=new StringBuilder();
		if( request.getParameter("roleName")!=null && !"".equals(request.getParameter("roleName"))){
			builder.append(" and name like '%"+request.getParameter("roleName")+"%'");
		}
		if(!ObjectUtils.isEmpty(request.getParameter("status"))){
			builder.append(" and status ='"+request.getParameter("status").toString()+"'");
		}
		return builder.toString();
	}
}
