package com.robin.example.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table="t_base_projectinfo")
@Data
public class ProjectInfo extends BaseObject {
	@MappingField(increment="1",primary="1")
	private Long id;
	@MappingField(field="proj_name")
	private String  projName;
	@MappingField(field="proj_code")
	private String projCode;
	@MappingField()
	private String description;
	@MappingField()
	private String company;
	@MappingField(field="use_springmvc")
	private String useMvc;
	@MappingField(field="struts_version")
	private String strutsVersion;
	@MappingField(field="spring_version")
	private String springVersion;
	@MappingField(field="presist_type")
	private String presistType;
	@MappingField(field="project_basepath")
	private String projBasePath;
	@MappingField(field="src_basepath")
	private String srcBasePath;
	@MappingField(field="dao_configfile")
	private String daoConfigFile;
	@MappingField(field="service_configfile")
	private String serviceConfigPath;
	@MappingField(field="web_basepath")
	private String webBasePath;
	@MappingField(field="use_annotation")
	private String useAnnotation;
	@MappingField(field="use_maven")
	private String useMaven;
	@MappingField(field="webframe_id")
	private Long webFrameId;
	@MappingField(field="datasource_id")
	private Long dataSourceId;
	@MappingField(field="proj_type")
	private Long projType;
	@MappingField
	private String author;
	@MappingField(field="annotation_package")
	private String annotationPackage;
	@MappingField(field="team_type")
	private String teamType;
	@MappingField(field="team_url")
	private String teamUrl;
	@MappingField(field = "jar_man_type")
	private String jarmanType;
	

}
