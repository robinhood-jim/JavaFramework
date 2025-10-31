package com.robin.core.web.serverless;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.lang.invoke.MethodHandle;
import java.util.List;

@Getter
@Setter
public class DynamicFunction {
    private MethodHandle invokeMethod;
    private List<ServerlessParameter> parameters;
    private boolean funStatic;
    private List<String> callMethods;
    public DynamicFunction(MethodHandle handle,List<ServerlessParameter> parameters,boolean funStatic){
        this.invokeMethod=handle;
        this.parameters=parameters;
        this.funStatic=funStatic;
    }
    public DynamicFunction(MethodHandle handle,List<ServerlessParameter> parameters,boolean funStatic,String callMethods){
        this.invokeMethod=handle;
        this.parameters=parameters;
        this.funStatic=funStatic;
        if(!ObjectUtils.isEmpty(callMethods)){
            this.callMethods= Lists.newArrayList(callMethods.split(","));
        }
    }
    public boolean checkCallMethod(String callMethod){
        return ObjectUtils.isEmpty(callMethods) || callMethods.contains(callMethod.toLowerCase());
    }



}
