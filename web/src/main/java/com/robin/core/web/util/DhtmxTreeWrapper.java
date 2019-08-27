package com.robin.core.web.util;

import com.robin.core.base.util.Const;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DhtmxTreeWrapper {

	public static void WrappSingleTree(List<String> list,HttpServletRequest request,HttpServletResponse response) throws Exception{
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
		sb.append("<tree id=\"0\">");
		
		for (int i = 0; i < list.size(); i++) {
				sb.append("<item id=\"" + list.get(i) + "\" text=\"" + list.get(i) + "\"  child=\"1\">");
				sb.append("</item>");
		}
		sb.append("</tree>");
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache_Control", "no-cache");
		response.getWriter().write(sb.toString());
		response.getWriter().close();
	}
	public static String WrappSingleTreeXml(List<String> list) throws Exception{
		StringBuffer sb = new StringBuffer("");
		//sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
		sb.append("<tree id=\"0\">");
		
		for (int i = 0; i < list.size(); i++) {
				sb.append("<item id=\"" +list.get(i)+ "\" text=\"" + list.get(i) + "\" child=\"1\" >");
				sb.append("</item>");
		}
		sb.append("</tree>");
		return sb.toString();
	}
	public static Map<String,Object> WrappObjectTreeRetJson(List<?> list,String pid,String key,String valuekey,String userdatakeys,String leafKey) throws Exception{
		String[] arr=null;
		if(userdatakeys!=null && !"".equals(userdatakeys))
			arr=userdatakeys.split(",");
		Map<String,Object> retmap=new HashMap<String, Object>();
		retmap.put("id", pid);
		List<Map<String,Object>> itemlist=new ArrayList<Map<String,Object>>();
		for (int i = 0; i < list.size(); i++) {
			Object org=list.get(i);
			Object keyobj=PropertyUtils.getProperty(org, key);
			Object valueobj=PropertyUtils.getProperty(org, valuekey);
			Map<String,Object> insertMap=new HashMap<String, Object>();
			insertMap.put("id", keyobj);
			insertMap.put("text", valueobj);
			if(arr!=null && arr.length>0){
				List<Map<String,Object>> userdatalist=new ArrayList<Map<String,Object>>();
				for (int j = 0; j < arr.length; j++) {
					Object val1=PropertyUtils.getProperty(org, arr[j]);
					Map<String,Object> userMap=new HashMap<String, Object>();
					userMap.put("name", arr[j]);
					userMap.put("content", val1);
					userdatalist.add(userMap);
				}
				insertMap.put("userdata", userdatalist);
			}
			if(leafKey!=null && !leafKey.isEmpty()){
				Object val1=PropertyUtils.getProperty(org, leafKey);
				if(val1!=null){
					if(val1.toString().equals(Const.VALID)){
						insertMap.put("open", "1");
					}else {
						insertMap.put("child", "1");
					}
				}
			}else{
				insertMap.put("child", "1");
			}
			itemlist.add(insertMap);
		}
		retmap.put("item", itemlist);
		return retmap;
	}
	public static String WrappObjectTreeRetXml(List<?> list,String parentkey,String key,String value,String userdatakeys,boolean hasChild) throws Exception{
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
		sb.append("<tree id=\""+parentkey+"\">");
		String[] arr=null;
		if(userdatakeys!=null && !"".equals(userdatakeys))
			arr=userdatakeys.split(",");
		for (int i = 0; i < list.size(); i++) {
			Object org=list.get(i);
			Object keyobj= BeanUtils.getProperty(org, key);
			Object valueobj=BeanUtils.getProperty(org, value);
			sb.append("<item id=\"" + keyobj.toString() + "\" text=\"" + valueobj.toString() + "\"");
			if(hasChild)
				sb.append(" child=\"1\">");
			else
				sb.append(" child=\"0\">");
			sb.append("<userdata name=\"parentid\">"+parentkey+"</userdata>");
			if(arr!=null && arr.length>0){
				for (int j = 0; j < arr.length; j++) {
					Object val1=BeanUtils.getProperty(org, arr[j]);
					sb.append("<userdata name=\""+arr[j]+"\">"+val1.toString()+"</userdata>");
				}
			}
			sb.append("</item>");
		}
		sb.append("</tree>");
		return sb.toString();
		
	}
	public static void WrappObjectTree(List<?> list,String parentkey,String key,String value,String userdatakeys,HttpServletRequest request,HttpServletResponse response) throws Exception{
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
		sb.append("<tree id=\""+parentkey+"\">");
		String[] arr=null;
		if(userdatakeys!=null && !"".equals(userdatakeys))
			arr=userdatakeys.split(",");
		for (int i = 0; i < list.size(); i++) {
			Object org=list.get(i);
			Object keyobj=PropertyUtils.getProperty(org, key);
			Object valueobj=PropertyUtils.getProperty(org, value);
			sb.append("<item id=\"" + keyobj.toString() + "\" text=\"" + valueobj.toString() + "\" child=\"1\">");
			sb.append("<userdata name=\"parentid\">"+parentkey+"</userdata>");
			if(arr!=null && arr.length>0){
				for (int j = 0; j < arr.length; j++) {
					Object val1=PropertyUtils.getProperty(org, arr[j]);
					sb.append("<userdata name=\""+arr[j]+"\">"+val1.toString()+"</userdata>");
				}
			}
			sb.append("</item>");
		}
		sb.append("</tree>");
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache_Control", "no-cache");
		response.getWriter().write(sb.toString());
		response.getWriter().close();
		
	}

}
