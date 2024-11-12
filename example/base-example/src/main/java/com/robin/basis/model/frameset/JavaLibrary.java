package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value ="t_base_javalibrary")
@Data
public class JavaLibrary extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(value ="library_name")
	private String name;
	@MappingField
	private String version;
	@MappingField(value ="zip_file")
	private String zipFile;

	
	
}
