package com.robin.webui.contorller.system;


import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.webui.util.AuthUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/system/menu")
public class SysResourceContorller  {


	@RequestMapping("/list")
	@ResponseBody
	public Map<String,Object> list(HttpServletRequest request,
			HttpServletResponse response) {

		return RestTemplateUtils.getResultFromRestUrl("product/system/menu/list",new Object[]{}, AuthUtils.getRequestParam(null,request));
	}
	@RequestMapping("/show")
	public ModelAndView show(ModelMap model, HttpServletRequest request,
							 HttpServletResponse response){
		return new ModelAndView("/menu/show_menu");
	}
	@RequestMapping("/save")
	@ResponseBody
	public Map<String, Object> saveMenu(HttpServletRequest request,
			HttpServletResponse response){
		return RestTemplateUtils.postFromRestUrl("system/menu/save", AbstractController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
	}
	
	@RequestMapping("/showrole")
	public ModelAndView showAssignRole(HttpServletRequest request,HttpServletResponse response){
		
		Map<String,Object> retMap=RestTemplateUtils.getResultFromRestUrl("system/menu/showrole",new Object[]{},AuthUtils.getRequestParam(null,request));
		request.setAttribute("roleList", retMap.get("roleList"));
		request.setAttribute("avaliableList", retMap.get("avaliableList"));
		request.setAttribute("resId", retMap.get("resId"));
		return new ModelAndView("/menu/assign_role");
	}
	@RequestMapping("/edit")
	@ResponseBody
	public Map<String,Object> queryUser(HttpServletRequest request,
			HttpServletResponse response){

		return RestTemplateUtils.getResultFromRestUrl("system/menu/edit",new Object[]{},AuthUtils.getRequestParam(null,request));
	}
	@RequestMapping("/update")
	@ResponseBody
	public Map<String, Object> updateSysResource(HttpServletRequest request,
			HttpServletResponse response){
		return RestTemplateUtils.postFromRestUrl("system/menu/update", AbstractController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
	}
	@RequestMapping("/assignrole")
	@ResponseBody
	public Map<String, Object> assignRole(HttpServletRequest request,HttpServletResponse response){

		return RestTemplateUtils.postFromRestUrl("system/menu/assignrole", AbstractController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
	}
}
