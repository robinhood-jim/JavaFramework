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
package com.robin.core.base.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.exception.DAOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

/**
 * <p>Description:<b>Model VO BaseObject,All DataObject should Override this class</b></p>
 *
 */
@Slf4j
public abstract class BaseObject implements Serializable,Cloneable{
	private static final long serialVersionUID = -1156095048376157515L;
	private List<String> dirtyColumnList=new ArrayList<String>();

	
	public BaseObject()
    {
    }
	/**
	 * Make dirty to update
	 * @param key
	 */
	public void AddDirtyColumn(String key){
		dirtyColumnList.add(key);	
	}
	public List<String> getDirtyColumn(){
		return dirtyColumnList;
	}
	@Override
    public String toString(){
		String str="";
		Gson gson=new Gson();
		try{
		str=gson.toJson(this);
		}catch(Exception ex){
			log.error("",ex);
		}
		return str;
	}
	public boolean isEmpty() throws DAOException {
		boolean emptyTag=true;
		List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(getClass());
		try {
			for (FieldContent content : fields) {
				if (!ObjectUtils.isEmpty(content.getGetMethod().invoke(this))) {
					emptyTag = false;
					break;
				}
			}
		}catch (InvocationTargetException | IllegalAccessException ex){
			throw new DAOException(ex);
		}
		return emptyTag;
	}
}
