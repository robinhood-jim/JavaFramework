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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_sys_resource_info")
public class SysResource extends AbstractMybatisModel {
	@TableId(type = IdType.AUTO)
	private Long id;
	private String resName;
	private String resType;
	private String url;
	private Long powerId;
	@TableField("is_leaf")
	private Integer leafTag;
	private String status;
	@TableField("res_code")
	private String code;
	private Long pid;
	private Integer seqNo;
	private String remark;
	private Long orgId;
	private String permission;
	private Long tenantId;
	private String icon;
	private String routerPath;

}
