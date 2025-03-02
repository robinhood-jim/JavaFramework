package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_maven_dependency_collect")
@Data
public class MavenDependencyGroup extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private Long dependencyId;
    private String version;
    private String name;
    private Integer useVersion;
    private Integer ifValid;
    private String dependencies;


}
