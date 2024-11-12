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
@MappingEntity(value ="t_meta_hadoop_cfg")
@Data
public class HadoopClusterDef extends BaseObject{
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField
	private String name;
	@MappingField(value ="zk_ips")
	private String zkIps;
	@MappingField(value ="zk_port")
	private Integer zkPort;
	@MappingField(value ="hive_server_ip")
	private String hiveServerIp;
	@MappingField(value ="hive_server_port")
	private Integer hiveServerPort;
	@MappingField(value ="hive_server_user")
	private String hiveServerUser;
	@MappingField(value ="hive_server_pwd")
	private String hiveServerPwd;
	@MappingField(value ="hdfs_server_ip")
	private String hdfsServerIp;
	@MappingField(value ="hdfs_server_port")
	private Integer hdfsServerPort;
	@MappingField(value ="is_ha")
	private String haTag;
	@MappingField(value ="ha_nameserver")
	private String haNameServer;
	@MappingField(value ="standby_server")
	private String standByServer;
	@MappingField(value ="mr_frame")
	private String mrFrame;
	@MappingField(value ="yarn_resource_ips")
	private String yarnResIps;
	@MappingField(value ="yarn_resource_port")
	private Integer yarnResPort;
	@MappingField(value = "resource_id")
	private Long resId;

}

