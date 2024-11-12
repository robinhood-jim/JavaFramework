package com.robin.basis.model.system;


import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.basis.model.AbstractModel;
import lombok.Data;

import java.time.LocalDateTime;

@MappingEntity(value = "t_application_version")
@Data
public class ApplicationVersion extends AbstractModel {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField
    private Long appId;
    @MappingField
    private String version;
    @MappingField
    private String iconUrl;
    @MappingField
    private String description;
    @MappingField
    private Integer userLimit;
    @MappingField
    private Integer currentUsers;
    @MappingField
    private LocalDateTime validateFrom;
    @MappingField
    private LocalDateTime validateTo;

}
