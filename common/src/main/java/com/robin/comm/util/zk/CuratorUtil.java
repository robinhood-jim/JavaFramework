package com.robin.comm.util.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;

import java.util.List;


public class CuratorUtil {
	public static List<String> getSubPath(CuratorFramework client,String basePath) throws Exception{
		List<String> list=client.getChildren().forPath(basePath);
		return list;
	}
	public static void createZNode(CuratorFramework client,String path,String value) throws Exception{
		if(client.checkExists().forPath(path)==null){
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		}else{
			client.delete().forPath(path);
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		}
		if(value!=null && !"".equals(value)){
			client.setData().forPath(path, value.getBytes());
		}
	}
	public static void createZNodeWithWatcher(CuratorFramework client, String path, CuratorWatcher watcher) throws Exception{
		if(client.checkExists().forPath(path)==null){
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		}else{
			client.delete().forPath(path);
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		}
		client.getData().usingWatcher(watcher).forPath(path);
	}
	public static void deleteZnode(CuratorFramework client,String path) throws Exception{
		if(client.checkExists().forPath(path)!=null){
			client.delete().forPath(path);
		}
	}

}
