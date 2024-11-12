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
package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value ="t_base_projectinfo")
@Data
public class ProjectInfo extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(value ="proj_name")
	private String  projName;
	@MappingField(value ="proj_code")
	private String projCode;
	@MappingField()
	private String description;
	@MappingField()
	private String company;
	@MappingField(value ="use_springmvc")
	private String useMvc;
	@MappingField(value ="struts_version")
	private String strutsVersion;
	@MappingField(value ="spring_version")
	private String springVersion;
	@MappingField(value ="persist_type")
	private String persistType;
	@MappingField(value ="project_basepath")
	private String projBasePath;
	@MappingField(value ="src_basepath")
	private String srcBasePath;
	@MappingField(value ="dao_configfile")
	private String daoConfigFile;
	@MappingField(value ="service_configfile")
	private String serviceConfigPath;
	@MappingField(value ="web_basepath")
	private String webBasePath;
	@MappingField(value ="use_annotation")
	private String useAnnotation;
	@MappingField(value ="use_maven")
	private String useMaven;
	@MappingField(value ="webframe_id")
	private Long webFrameId;
	@MappingField(value ="datasource_id")
	private Long dataSourceId;
	@MappingField(value ="proj_type")
	private Long projType;
	@MappingField
	private String author;
	@MappingField(value ="annotation_package")
	private String annotationPackage;
	@MappingField(value ="team_type")
	private String teamType;
	@MappingField(value ="team_url")
	private String teamUrl;
	@MappingField(value = "jar_man_type")
	private String jarmanType;
	@MappingField(value = "credential_id")
	private Long credentialId;

}
