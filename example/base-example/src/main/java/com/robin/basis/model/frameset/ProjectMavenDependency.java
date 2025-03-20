package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.model.BaseObject;
import lombok.Getter;
import lombok.Setter;

@MappingEntity("t_project_maven_dependency")
@Getter
@Setter
public class ProjectMavenDependency extends BaseObject {
    private Long id;
    private Long projId;
    private String jdkVersion;
    private String compilePluginVersion;
    private String jarPluginVersion;
    private String dependencyPluginVersion;
    private String resourcePluginVersion;
    private String sourcePluginVersion;
    private Integer version;
    private String dependencies;

}
