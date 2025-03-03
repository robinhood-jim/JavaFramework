package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
public class MavenDependency extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private String groupId;
    private String artifactId;
    private String version;



}
