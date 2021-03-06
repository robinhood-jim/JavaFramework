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
package com.robin.core.base.spring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

public class DynamicResource extends AbstractResource implements Resource {
	private AbstractDynamicBean dynamicBean;
    
    public DynamicResource(AbstractDynamicBean dynamicBean){
        this.dynamicBean = dynamicBean;  
    }  
    /* (non-Javadoc) 
     * @see org.springframework.core.io.InputStreamSource#getInputStream() 
     */  
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(dynamicBean.getXml().getBytes(StandardCharsets.UTF_8));
    }

	@Override
    public String getDescription() {
		
		return "Dynamic";
	}
	

}
