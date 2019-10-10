package com.robin.core.query.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 锟斤拷锟斤拷锟斤拷锟斤拷锟捷讹拷锟斤拷转锟斤拷锟轿狢ondition元锟斤拷,锟斤拷锟斤拷Condition锟斤拷CondidtionList
 * @author AndyLee
 *
 */
public class ConditionBuilder {

	public static ConditionList build(Object obj1,int empty,Map methods)
		throws Exception {
		if(obj1 == null){
			throw new IllegalArgumentException("parameter is null!");
		}
		Method[] method = obj1.getClass().getMethods();
		String methodName;
		String methodFix;
		
		ConditionList list = new ConditionList(); 
		for (int i = 0; i < method.length; i++) {
			methodName = method[i].getName();
			methodFix = methodName.substring(3);
			if (methodName.startsWith("get") && !"getClass".equals(methodName) && (methods == null || methods.isEmpty()
					|| methods.containsKey(methodFix))) {
				if (method[i].getParameterTypes().length > 0){
					continue;
				}
				Object[] args1 = new Object[0];
				Object[] args2 = new Object[1];
				args2[0] = method[i].invoke(obj1, args1);
				if(empty == 1 && args2[0] == null){
					continue;
				}
				if(empty == 2 && (args2[0] == null || "".equals(args2[0]) || args2[0].equals(0)
						|| args2[0].equals(0L) || args2[0].equals(0.0)
						|| args2[0].equals(new Float(0))) || args2[0].equals(new HashMap()) || args2[0].equals(new ArrayList())){
					continue;
				}
				
				Condition condition = null;
				if (methods != null && !methods.isEmpty() && methods.get(methodFix) != null && !"".equals(methods.get(methodFix))){
					condition = new Condition(methodFix,(String)methods.get(methodFix),args2[0]);
				}else{
					condition = new Condition(methodFix,Condition.EQUALS,args2[0]);
				}
				list.add(condition);
				
			}
		}
		return list;
	}
}
