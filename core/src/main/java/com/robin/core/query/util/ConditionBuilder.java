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
	/**
	 * 锟斤拷锟斤拷Bean1锟斤拷锟斤拷锟斤拷锟斤拷缘锟紹ean2锟斤拷锟斤拷 只锟斤拷锟斤拷Methods锟斤拷锟节碉拷锟斤拷锟斤拷锟斤拷锟?
	 * 锟斤拷锟斤拷锟斤拷set锟斤拷锟斤拷锟叫讹拷值锟斤拷锟斤拷
	 * @param obj1 锟斤拷锟斤拷源锟斤拷锟斤拷
	 * @param obj2 锟斤拷锟斤拷目锟斤拷锟斤拷锟?
	 * @param emtpy 0 Null锟斤拷锟絅ull全锟斤拷锟斤拷锟狡ｏ拷1 锟斤拷锟斤拷锟狡凤拷锟斤拷值为Null锟斤拷锟斤拷锟斤拷 2 锟斤拷锟斤拷锟狡凤拷锟斤拷值为Null锟斤拷""锟斤拷0锟斤拷0.0锟斤拷锟斤拷List,锟斤拷Map锟斤拷锟斤拷锟斤拷 
	 * @param methods 锟斤拷要锟斤拷锟狡碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷椋拷锟斤拷诖硕锟斤拷锟斤拷锟斤拷械锟斤拷锟斤拷锟斤拷虿槐锟斤拷锟斤拷疲锟斤拷锟斤拷硕锟斤拷锟轿拷锟斤拷锟矫慈拷锟斤拷锟斤拷锟?put("UserName",Condidtion.EQUALS),put("UserPasswd",Condidtion.LIKE)
	 * @throws Exception
	 */
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
			methodFix = methodName.substring(3, methodName.length());
			if (methodName.startsWith("get") && !methodName.equals("getClass") && (methods == null || methods.isEmpty() 
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
				if(empty == 2 && (args2[0] == null || args2[0].equals("") || args2[0].equals(new Integer(0))
						|| args2[0].equals(new Long(0)) || args2[0].equals(new Double(0))
						|| args2[0].equals(new Float(0))) || args2[0].equals(new HashMap()) || args2[0].equals(new ArrayList())){
					continue;
				}
				
				Condition condition = null;
				if (methods != null && !methods.isEmpty() && methods.get(methodFix) != null && !methods.get(methodFix).equals("")){
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
