package com.robin.example.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table="t_base_entitymapping")
@Data
public class EntityMapping extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(field="proj_id")
	private Long projId;
	@MappingField(field="source_id")
	private Long sourceId;
	@MappingField(field="db_schema")
	private String dbschema;
	@MappingField(field="entity_code")
	private String entityCode;
	@MappingField()
	private String description;
	@MappingField(field="java_class")
	private String javaClass;
	@MappingField(field="spring_name")
	private String springName;
	@MappingField(field="dao_packagename")
	private String daoPackage;
	@MappingField(field="model_packagename")
	private String modelPackage;
	@MappingField(field="service_packagename")
	private String servicePackage;
	@MappingField(field="web_packagename")
	private String webPackage;
	@MappingField(field="dao_configpath")
	private String daoconfigPath;
	@MappingField(field="service_configpath")
	private String serviceconfigPath;
	@MappingField(field="web_configpath")
	private String webconfigPath;
	@MappingField(field="web_path")
	private String webPath;
	@MappingField(field="page_path")
	private String pagePath;
	@MappingField(field="pk_type")
	private String pkType;
	@MappingField(field="gen_type")
	private String genType;
	@MappingField(field="sequence_name")
	private String seqName;
	@MappingField
	private String name;

	
}
