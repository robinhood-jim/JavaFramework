package com.robin.core.fileaccess.cache;

import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.holder.BufferedReaderHolder;
import com.robin.core.fileaccess.holder.InputStreamHolder;
import com.robin.core.fileaccess.holder.OutputStreamHolder;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;

import java.lang.reflect.Constructor;
import java.util.*;


public class CacheHolder {
	private int inlimitstreamnum=10000;
	private int outlimitstreamnum=100;
	private Map<String, AbstractResourceAccessUtil> resouceAccessUtilMap=new HashMap<String, AbstractResourceAccessUtil>();
	private List<InputStreamHolder> inputStreamContainer=null;
	private List<OutputStreamHolder> outStreanContainer=null;
	private List<BufferedReaderHolder> readerContainer=null;
	private static CacheHolder instance=null;

	private CacheHolder() {
		ResourceBundle bundle=ResourceBundle.getBundle("sysconfig");
		if(bundle.containsKey("inputstreamlimit")){
			inlimitstreamnum=Integer.parseInt(bundle.getString("inputstreamlimit"));
		}
		if(bundle.containsKey("outputstreamlimit")){
			outlimitstreamnum=Integer.parseInt(bundle.getString("outputstreamlimit"));
		}
		inputStreamContainer=new ArrayList<InputStreamHolder>(inlimitstreamnum);
		readerContainer=new ArrayList<BufferedReaderHolder>(inlimitstreamnum);
		String[] fileaccessType=bundle.getString("fileaccess_type").split(",");
		for (int i = 0; i < fileaccessType.length; i++) {
			AbstractResourceAccessUtil util=loadResourceUtil(fileaccessType[i]);
			if(util!=null)
				resouceAccessUtilMap.put(fileaccessType[i].toLowerCase(),util);
		}	
	}
	
	public static CacheHolder getInstance(){
		if(instance==null){
			synchronized (CacheHolder.class) {
				if(instance==null){
					instance=new CacheHolder();
				}
			}
		}
		return instance;
	}
	
	private AbstractResourceAccessUtil loadResourceUtil(String type){
		AbstractResourceAccessUtil util=null;
		try{
			String className="com.robin.core.fileaccess.util."+StringUtils.initailCharToUpperCase(type)+"ResourceAccessUtil";
			Class<?> clazz= Class.forName(className);
			Constructor<?> construct=clazz.getDeclaredConstructor(new Class[]{});
			util= (AbstractResourceAccessUtil) construct.newInstance(new Object[]{});
		}catch(Exception ex){
			
		}
		return util;
	}
	public InputStreamHolder getAvaliableInputStreamHolder(){
		InputStreamHolder holder=null;
		synchronized (inputStreamContainer) {
			if(inputStreamContainer.size()<inlimitstreamnum){
				holder=new InputStreamHolder();
				inputStreamContainer.add(holder);
			}else{
				//get a holder from pool
				for (InputStreamHolder tholder:inputStreamContainer) {
					if(tholder.getBusyTag()){
						holder=tholder;
						tholder.setBusyTag(true);
					}
				}
			}
		}
		return holder;
	}
	public AbstractResourceAccessUtil getAccessUtilByProtocol(String protocol){
		if(resouceAccessUtilMap.containsKey(protocol)){
			return resouceAccessUtilMap.get(protocol);
		}else{
			return null;
		}
	}
	
}
