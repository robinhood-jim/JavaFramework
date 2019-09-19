/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.fileaccess.mananger;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.robin.core.base.exception.ResourceNotAvailableException;
import com.robin.core.fileaccess.pool.ResourceAccessHolder;
import com.robin.core.fileaccess.holder.InputStreamHolder;
import com.robin.core.fileaccess.holder.OutputStreamHolder;

/**
 * <p>Project:  core</p>
 *
 * <p>Description:ResourceAccessManager.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月17日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ResourceAccessManager {
	private int waitSecond=60;
	private int retryNums=5;
	private List<InputStreamHolder> inputStreamContainer=null;
	private List<OutputStreamHolder> outStreanContainer=null;
	private static ResourceAccessManager manager;
	private ResourceAccessManager(){
		ResourceBundle bundle=ResourceBundle.getBundle("sysconfig");
		if(bundle.containsKey("RESOURCEGETWAIT")){
			waitSecond=Integer.parseInt(bundle.getString("RESOURCEGETWAIT"));
		}
		if(bundle.containsKey("RESOURCEGETRETRYS")){
			retryNums=Integer.parseInt(bundle.getString("RESOURCEGETRETRYS"));
		}
		
	}
	
	public InputStreamHolder getInputStreamByType(Map<String, Object> configMap,String path,String encode) throws Exception{
		InputStreamHolder holder= ResourceAccessHolder.getInstance().getAvaliableInputStreamHolder();
		int pos=0;
		//can  not get avaliabe Holder,Sleep and wait
		while(holder==null && pos<retryNums){
			 Thread.sleep(waitSecond*1000);
			 holder= ResourceAccessHolder.getInstance().getAvaliableInputStreamHolder();
		}
		if(holder==null)
			throw new ResourceNotAvailableException("InputStream over limit,Please wait");
		return holder;
	}
	
}
