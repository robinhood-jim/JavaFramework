package com.robin.example.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table="t_base_fieldmapping")
@Data
public class FieldMapping extends BaseObject{
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(field="proj_id")
	private Long projId;
	@MappingField(field="source_id")
	private Long sourceId;
	@MappingField(field="entity_id")
	private Long entityId;
	@MappingField(field="field_code")
	private String code;
	@MappingField(field="data_type")
	private String dataType;
	@MappingField(field="mapping_field")
	private String field;
	@MappingField(field="mapping_type")
	private String type;
	@MappingField(field="is_primary")
	private String isPrimary;
	@MappingField(field="is_genkey")
	private String isGenkey;
	@MappingField(field="is_sequence")
	private String isSequnce;
	@MappingField(field="sequence_name")
	private String seqName;
	@MappingField(field="is_nullable")
	private String isNull;
	@MappingField
	private String name;
	@MappingField(field="display_type")
	private String displayType;
	@MappingField(field="show_in_grid")
	private String showIngrid;
	@MappingField(field="show_in_query")
	private String showInquery;
	@MappingField(field="is_editable")
	private String editable;
	
	

}
