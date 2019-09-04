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
package com.robin.meta.model.resource;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@SuppressWarnings("serial")
@MappingEntity(table="t_meta_hadoop_cfg")
@Data
public class HadoopClusterDef extends BaseObject{
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField
	private String name;
	@MappingField(field="zk_ips")
	private String zkIps;
	@MappingField(field="zk_port")
	private Integer zkPort;
	@MappingField(field="hive_server_ip")
	private String hiveServerIp;
	@MappingField(field="hive_server_port")
	private Integer hiveServerPort;
	@MappingField(field="hive_server_user")
	private String hiveServerUser;
	@MappingField(field="hive_server_pwd")
	private String hiveServerPwd;
	@MappingField(field="hdfs_server_ip")
	private String hdfsServerIp;
	@MappingField(field="hdfs_server_port")
	private Integer hdfsServerPort;
	@MappingField(field="is_ha")
	private String haTag;
	@MappingField(field="ha_nameserver")
	private String haNameServer;
	@MappingField(field="standby_server")
	private String standByServer;
	@MappingField(field="mr_frame")
	private String mrFrame;
	@MappingField(field="yarn_resource_ips")
	private String yarnResIps;
	@MappingField(field="yarn_resource_port")
	private Integer yarnResPort;
	@MappingField(field = "resource_id")
	private Long resId;

}

