package com.robin.hadoop.hdfs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.robin.core.base.util.Const;



/**
 * <p>Project:  etl-common</p>
 *
 * <p>Description:HDFSProperty.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015-4-16</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class HDFSProperty {
	private String defaultName;
	private Map<String, String> haConfig=new HashMap<String, String>();
	
	public String getDefaultName() {
		return defaultName;
	}
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}
	public Map<String, String> getHaConfig() {
		return haConfig;
	}
	public void setHaConfig(Map<String, String> haConfig) {
		this.haConfig = haConfig;
		if(defaultName==null){
			if(haConfig.containsKey(Const.HDFS_NAME_HADOOP1))
				defaultName=haConfig.get(Const.HDFS_NAME_HADOOP1);
			else if(haConfig.containsKey(Const.HDFS_NAME_HADOOP2))
				defaultName=haConfig.get(Const.HDFS_NAME_HADOOP2);
		}
	}
	public void setHaConfigByObj(Map<String, Object> config) {
		Iterator<String> keyIterator=config.keySet().iterator();
		while(keyIterator.hasNext()){
			String key=keyIterator.next();
			haConfig.put(key, config.get(key).toString());
		}
		if(defaultName==null){
			if(haConfig.containsKey(Const.HDFS_NAME_HADOOP1))
				defaultName=haConfig.get(Const.HDFS_NAME_HADOOP1);
			else if(haConfig.containsKey(Const.HDFS_NAME_HADOOP2))
				defaultName=haConfig.get(Const.HDFS_NAME_HADOOP2);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean iseq=false;
		if(obj instanceof HDFSProperty){
			HDFSProperty comp= (HDFSProperty) obj;
			if(this.getDefaultName().equals(comp.getDefaultName())){
				Iterator<String> iter=this.getHaConfig().keySet().iterator();
				while(iter.hasNext()){
					String key=iter.next();
					if(comp.getHaConfig().containsKey(key)){
						if(!this.getHaConfig().get(key).equals(comp.getHaConfig().get(key))){
							return false;
						}
					}
				}
				iseq=true;
			}
		}
		return iseq;
	}
}
