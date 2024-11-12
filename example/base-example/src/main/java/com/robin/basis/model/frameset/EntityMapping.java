package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_base_entitymapping")
@Data
public class EntityMapping extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField
	private Long projId;
	@MappingField
	private Long sourceId;
	@MappingField
	private String dbSchema;
	@MappingField
	private String entityCode;
	@MappingField
	private String description;
	@MappingField
	private String javaClass;
	@MappingField
	private String springName;
	@MappingField("dao_packagename")
	private String daoPackage;
	@MappingField("model_packagename")
	private String modelPackage;
	@MappingField("service_packagename")
	private String servicePackage;
	@MappingField("web_packagename")
	private String webPackage;
	@MappingField
	private String daoConfigPath;
	@MappingField("service_configpath")
	private String serviceconfigPath;
	@MappingField("web_configpath")
	private String webconfigPath;
	@MappingField("web_path")
	private String webPath;
	@MappingField("page_path")
	private String pagePath;
	@MappingField
	private String pkType;
	@MappingField
	private String genType;
	@MappingField("sequence_name")
	private String seqName;
	@MappingField
	private String name;

	
}
