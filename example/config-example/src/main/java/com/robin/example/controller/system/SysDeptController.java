package com.robin.example.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.robin.core.web.controller.BaseContorller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.query.util.PageQuery;

@Controller
@RequestMapping("/system/dept")
public class SysDeptController extends BaseContorller {
	@Autowired
	private JdbcDao jdbcDao;
	@RequestMapping("/listjson")
	@ResponseBody
	public Map<String,Object> getdeptJson(HttpServletRequest request,HttpServletResponse response){
		String allowNull=request.getParameter("allowNull");
		boolean insertNullVal=true;
		if(allowNull!=null && !allowNull.isEmpty() && allowNull.equalsIgnoreCase("false")){
			insertNullVal=false;
		}
		PageQuery query=new PageQuery();
		query.setSelectParamId("GET_DEPTINFO");
		query.setPageSize(0);
		jdbcDao.queryBySelectId(query);
		Map<String, Object> map=new HashMap<String, Object>();
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		if(insertNullVal){
			insertNullSelect(list);
		}
		list.addAll(query.getRecordSet());
		map.put("options", list);
		return map;
	}
}
