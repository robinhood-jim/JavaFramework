package com.robin.core.web.serverless;

import io.github.classgraph.MethodInfo;
import io.github.classgraph.TypeSignature;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ServerlessParameter {
    private String paramName;
    private TypeSignature signature;

}
