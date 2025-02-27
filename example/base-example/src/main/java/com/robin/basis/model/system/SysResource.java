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
package com.robin.basis.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
@MappingEntity(value ="t_sys_resource_info")
public class SysResource extends BaseObject {
	@MappingField(primary=true,increment=true)
	private Long id;
	@MappingField(value ="res_name")
	private String name;
	@MappingField(value ="res_type")
	private String type;
	@MappingField
	private String url;
	@MappingField
	private Long powerId;
	@MappingField(value = "is_leaf")
	private Integer leafTag;
	@MappingField
	private String status;
	@MappingField(value ="res_code")
	private String code;
	@MappingField
	private String resId;
	@MappingField
	private Long pid;
	@MappingField
	private Integer seqNo;
	@MappingField
	private String remark;
	@MappingField
	private Long orgId;
	@MappingField
	private String permission;
	private Long tenantId;
	

}
