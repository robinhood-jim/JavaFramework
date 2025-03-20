package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.model.BaseObject;

@MappingEntity("t_maven_plugins")
public class MavenPlugins extends BaseObject {
    private Long id;
    private String groupId;
    private String artifactId;
    private String version;
}
