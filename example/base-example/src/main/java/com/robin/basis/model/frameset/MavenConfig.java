package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

@MappingEntity("t_maven_project_config")
public class MavenConfig extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private String jdkVersion;
    private String compilePluginVersion;
    private String jarPluginVersion;
    private String dependencyPluginVersion;
    private String resourcePluginVersion;
    private String sourcePluginVersion;



}
