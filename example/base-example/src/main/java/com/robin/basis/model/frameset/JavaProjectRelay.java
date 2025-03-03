package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value ="t_base_projrelay")
@Data
public class JavaProjectRelay extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(value ="proj_id")
	private Long projId;
	@MappingField(value ="library_id")
	private Long libraryId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProjId() {
		return projId;
	}
	public void setProjId(Long projId) {
		this.projId = projId;
	}
	public Long getLibraryId() {
		return libraryId;
	}
	public void setLibraryId(Long libraryId) {
		this.libraryId = libraryId;
	}
	

}
