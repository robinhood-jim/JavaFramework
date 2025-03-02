package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_maven_dependency_exclusion")
@Data
public class MavenDependencyExclusion extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private String groupId;
    private String artifactId;

}
