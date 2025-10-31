package com.robin.core.web.serverless;

import io.github.classgraph.MethodInfo;
import io.github.classgraph.TypeSignature;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;


@Getter
@Setter
public class ServerlessParameter {
    private String paramName;
    private TypeSignature signature;
    private Type javaType;

}
