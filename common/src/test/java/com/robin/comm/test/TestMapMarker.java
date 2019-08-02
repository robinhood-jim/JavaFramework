package com.robin.comm.test;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

public class TestMapMarker {
	public ConcurrentMap<String, Long> scriptMap=new MapMaker().initialCapacity(1000).weakKeys().makeMap();
	public static void main(String[] args){
		TestMapMarker marker=new TestMapMarker();
		marker.scriptMap.put("111", 123213L);
		marker.scriptMap.put("222", 123213L);
		marker.test();
	}
	public void test(){
		System.out.println(scriptMap.containsKey("111"));
		System.out.println(scriptMap.containsKey("222"));
	}
	

}
