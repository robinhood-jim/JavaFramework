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
package com.robin.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@SuppressWarnings("serial")
@MappingEntity(table="t_meta_global_resource")
@Data
public class GlobalResource extends BaseObject {
	@MappingField(increment="1",primary="1")
	private Long id;
	@MappingField(field="res_type")
	private Long resType;
	@MappingField(field="res_name")
	private String resName;
	@MappingField
	private String protocol;
	@MappingField(field="db_type")
	private String dbType;
	@MappingField(field="ip_address")
	private String ipAddress;
	@MappingField
	private Integer port;
	@MappingField(field="db_name")
	private String dbName;
	@MappingField(field="username")
	private String userName;
	@MappingField
	private String password;
	@MappingField(field="jdbc_url")
	private String jdbcUrl;
	@MappingField(field="is_pool")
	private String poolTag;
	@MappingField(field="max_active")
	private Integer maxActive;
	@MappingField(field="max_idle")
	private Integer maxIdle;
	@MappingField(field="file_path")
	private String filePath;
	@MappingField(field = "file_format")
	private String fileFormat;
	@MappingField(field="cluster_code")
	private String clusterCode;
	@MappingField(field = "record_content")
	private String recordContent;
	@MappingField(field = "authorize_type")
	private Integer authType;

	public static enum AuthorizeType{
		TYPE_PULIC(1),
		TYPE_PULICTOORG(2),
		TYPE_PUBLICTODEPT(3),
		TYPE_PUBLICTOCORP(4),
		TYPE_AUTH(5);
		private Integer value;
		private AuthorizeType(Integer value){
			this.value=value;
		}
		@Override
		public String toString(){
			return value.toString();
		}
	}
}

