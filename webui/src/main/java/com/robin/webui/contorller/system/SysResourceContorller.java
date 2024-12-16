package com.robin.webui.contorller.system;

import com.robin.webui.contorller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/system/menu")
public class SysResourceContorller extends BaseController {


	@PostMapping("/list")
	@ResponseBody
	public Map<String,Object> list(HttpServletRequest request,
			HttpServletResponse response) {

		return getResultFromRest(request,"system/menu/list");
	}
	@GetMapping("/show")
	public String show(ModelMap model,HttpServletRequest request,
			HttpServletResponse response){
		return "/menu/show_menu";
	}
	@PostMapping("/save")
	@ResponseBody
	public Map<String, Object> saveMenu(HttpServletRequest request,
			HttpServletResponse response){

		return getResultFromRest(request,"system/menu/save");
	}
	
	@GetMapping("/showrole")
	public String showAssignRole(HttpServletRequest request,HttpServletResponse response){
		
		Map<String,Object> retMap=getResultFromRest(request,"system/menu/showrole");
		request.setAttribute("roleList", retMap.get("roleList"));
		request.setAttribute("avaliableList", retMap.get("avaliableList"));
		request.setAttribute("resId", retMap.get("resId"));
		return "/menu/assign_role";
	}
	@GetMapping("/edit")
	@ResponseBody
	public Map<String,Object> queryUser(HttpServletRequest request,
			HttpServletResponse response){

		return getResultFromRest(request,"system/menu/edit");
	}
	@PostMapping("/update")
	@ResponseBody
	public Map<String, Object> updateSysResource(HttpServletRequest request,
			HttpServletResponse response){

		return getResultFromRest(request,"system/menu/update");
	}
	@PostMapping("/assignrole")
	@ResponseBody
	public Map<String, Object> assignRole(HttpServletRequest request,HttpServletResponse response){

		return getResultFromRest(request,"system/menu/assignrole");
	}
}
