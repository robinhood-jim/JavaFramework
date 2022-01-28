package com.robin.basis.model.user;


import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.basis.model.AbstractModel;
import lombok.Data;

@MappingEntity(table = "t_customer_info")
@Data
public class Customer extends AbstractModel {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField
    private String name;
    @MappingField
    private String displayName;
    @MappingField
    private String address;
    @MappingField
    private String custType;
    @MappingField
    private Long manipulateOrgId;
    @MappingField
    private String status;
    @MappingField
    private String email;
    @MappingField
    private String phoneNum;
    @MappingField
    private Long userId;

}
