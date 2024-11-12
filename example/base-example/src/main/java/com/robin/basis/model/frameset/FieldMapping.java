package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value ="t_base_fieldmapping")
@Data
public class FieldMapping extends BaseObject{
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(value ="proj_id")
	private Long projId;
	@MappingField(value ="source_id")
	private Long sourceId;
	@MappingField(value ="entity_id")
	private Long entityId;
	@MappingField(value ="field_code")
	private String code;
	@MappingField(value ="data_type")
	private String dataType;
	@MappingField(value ="mapping_field")
	private String field;
	@MappingField(value ="mapping_type")
	private String type;
	@MappingField(value ="is_primary")
	private String isPrimary;
	@MappingField(value ="is_genkey")
	private String isGenkey;
	@MappingField(value ="is_sequence")
	private String isSequnce;
	@MappingField(value ="sequence_name")
	private String seqName;
	@MappingField(value ="is_nullable")
	private String isNull;
	@MappingField
	private String name;
	@MappingField(value ="display_type")
	private String displayType;
	@MappingField(value ="show_in_grid")
	private String showIngrid;
	@MappingField(value ="show_in_query")
	private String showInquery;
	@MappingField(value ="is_editable")
	private String editable;
	
	

}
