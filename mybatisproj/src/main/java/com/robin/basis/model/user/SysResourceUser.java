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
package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value = "t_sys_resource_user_r")
@TableName("t_sys_resource_user_r")
@Data
public class SysResourceUser extends AbstractMybatisModel {

	// primary key
	@MappingField(primary = true, increment = true)
	@TableId(type = IdType.AUTO)
	private Long id; //
	@MappingField(value = "res_id")
	private Long resId;
	@MappingField(value = "user_id")
	private Long userId;
	@MappingField(value ="assign_type")
	private Integer assignType;

	public static final int ASSIGN_ADD = 1;
	public static final int ASSIGN_DEL = 2;


	public static final String REF_CLASS = "SysResourceUser";
	public static final String PROP_STATUS = "status";
	public static final String PROP_RES_ID = "resId";
	public static final String PROP_USER_ID = "userId";
	public static final String PROP_ID = "id";

	public static final String REF_TABLE = "t_sys_resource_user_r";
	public static final String COL_STATUS = "STATUS";
	public static final String COL_RES_ID = "RES_ID";
	public static final String COL_USER_ID = "USER_ID";
	public static final String COL_ID = "ID";


}
