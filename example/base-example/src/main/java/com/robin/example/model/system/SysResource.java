package com.robin.example.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

@MappingEntity(table="t_sys_resource_info")
public class SysResource extends BaseObject {
	@MappingField(primary="1",increment="1")
	private Long id;
	@MappingField(field="res_name")
	private String name;
	@MappingField(field="res_type")
	private String type;
	@MappingField
	private String url;
	@MappingField(field="power_id")
	private Long powerId;
	@MappingField
	private String status;
	@MappingField(field="res_code")
	private Long code;
	@MappingField
	private Long pid;
	@MappingField(field="seq_no")
	private String seqNo;
	@MappingField
	private String remark;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Long getPowerId() {
		return powerId;
	}
	public void setPowerId(Long powerId) {
		this.powerId = powerId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getCode() {
		return code;
	}
	public void setCode(Long code) {
		this.code = code;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	

}
