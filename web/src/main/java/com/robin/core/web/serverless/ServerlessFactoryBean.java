package com.robin.core.web.serverless;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robin.core.base.annotation.ServerlessFunction;
import com.robin.core.base.annotation.ServerlessMethodParam;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.web.controller.AbstractController;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServerlessFactoryBean implements InitializingBean {
    String defaultScanPackage = "com.robin";
    private ScanResult scanResult;
    private final Map<String, Triple<MethodHandle, List<ServerlessParameter>, Boolean>> serverlessFunMap = new HashMap<>();
    private final Map<String, MethodHandle> prototypeHandlerMap = new HashMap<>();
    private final Map<String, Class<?>> functionOriginClassMap = new HashMap<>();
    private final Map<String, Object> userDefinedObjectMap = new HashMap<>();
    private final Map<String, Pair<String, MethodHandle>> initFuncMap = new HashMap<>();
    private final Gson gson = new Gson();

    @Override
    public void afterPropertiesSet() throws Exception {
        doInit();
    }

    public void doInit() {
        Environment environment = SpringContextHolder.getBean(Environment.class);
        String scanPackage = environment != null && environment.containsProperty("project.serverless.basePackage") ?
                environment.getProperty("project.serverless.basePackage") : defaultScanPackage;
        ClassGraph graph = new ClassGraph().enableAllInfo();
        if (!ObjectUtils.isEmpty(scanPackage)) {
            graph.whitelistPackages(scanPackage);
        }
        scanResult = graph.scan();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> scanResult.close()));
        ClassInfoList list = scanResult.getClassesWithMethodAnnotation(ServerlessFunction.class.getName());
        for (ClassInfo info : list) {
            MethodInfoList methodInfos = info.getMethodInfo();
            for (MethodInfo methodInfo : methodInfos) {
                boolean isStatic = methodInfo.isStatic();
                if (methodInfo.hasAnnotation(ServerlessFunction.class.getName())) {
                    AnnotationInfo methodAnnoInfo = methodInfo.getAnnotationInfo(ServerlessFunction.class.getName());
                    String funName = methodInfo.getName();
                    String initFunc = null;
                    String initParam = null;
                    AnnotationParameterValueList parameterValues = methodAnnoInfo.getParameterValues();
                    if (!parameterValues.isEmpty()) {
                        for (AnnotationParameterValue parameterValue : parameterValues) {
                            if (parameterValue.getName().equals("value") && !ObjectUtils.isEmpty(parameterValue.getValue())) {
                                funName = parameterValue.getValue().toString();
                            } else if (parameterValue.getName().equalsIgnoreCase("initFunc") && !ObjectUtils.isEmpty(parameterValue.getValue())) {
                                initFunc = parameterValue.getValue().toString();
                            } else if (parameterValue.getName().equalsIgnoreCase("initParam") && !ObjectUtils.isEmpty(parameterValue.getValue())) {
                                initParam = parameterValue.getValue().toString();
                            }
                        }
                    }
                    MethodParameterInfo[] parameterInfos = methodInfo.getParameterInfo();
                    if (parameterInfos != null && parameterInfos.length > 0) {
                        List<ServerlessParameter> methodParameterInfoList = new ArrayList<>();
                        for (MethodParameterInfo parameterInfo : parameterInfos) {
                            AnnotationInfo paramAnnoInfo = parameterInfo.getAnnotationInfo(ServerlessMethodParam.class.getName());
                            ServerlessParameter parameter = new ServerlessParameter();
                            parameter.setParamName(parameterInfo.getName());
                            parameter.setSignature(parameterInfo.getTypeSignatureOrTypeDescriptor());
                            if (paramAnnoInfo != null) {
                                parameter.setParamName(paramAnnoInfo.getParameterValues().get(0).getValue().toString());
                            }
                            methodParameterInfoList.add(parameter);
                        }
                        serverlessFunMap.put(funName, Triple.of(getMethodHandle(methodInfo), methodParameterInfoList, isStatic));
                    } else {
                        serverlessFunMap.put(funName, Triple.of(getMethodHandle(methodInfo), null, isStatic));
                    }
                    if (!ObjectUtils.isEmpty(initFunc) && !methodInfo.isStatic()) {
                        Object obj = loadBeanOrUserDefined(funName);
                        try {
                            if (obj != null) {
                                Method method = obj.getClass().getMethod(initFunc, Map.class);
                                if (!ObjectUtils.isEmpty(initParam)) {
                                    initFuncMap.put(funName, Pair.of(initParam, getMethodHandle(method)));
                                }else{
                                    initFuncMap.put(funName, Pair.of(null, getMethodHandle(method)));
                                }
                            }
                        } catch (Exception ex) {
                            throw new MissingConfigException(ex);
                        }
                    }
                    functionOriginClassMap.put(funName, info.loadClass());
                }
            }

        }
    }

    private MethodHandle getMethodHandle(MethodInfo method) {
        try {
            return MethodHandles.publicLookup().unreflect(method.loadClassAndGetMethod());
        } catch (IllegalAccessException ex) {
            log.error("{}", ex.getMessage());
            throw new MissingConfigException(ex.getMessage());
        }
    }

    private MethodHandle getMethodHandle(Method method) {
        try {
            return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            log.error("{}", ex.getMessage());
            throw new MissingConfigException(ex.getMessage());
        }
    }

    public void registerServerlessFunction(String functionName, IUserDefineServerlessFunction serverlessFunction) throws NoSuchMethodException, IllegalAccessException {
        if (serverlessFunMap.containsKey(functionName)) {
            throw new MissingConfigException("function " + functionName + " already defined!");
        } else {
            IUserDefineServerlessFunction userFuncObj = (IUserDefineServerlessFunction) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{IUserDefineServerlessFunction.class},
                    (proxy, method, args) -> serverlessFunction.doFunction((Map<String, Object>) args[0]));
            userDefinedObjectMap.put(functionName, userFuncObj);
            MethodHandle handler = MethodHandles.publicLookup().unreflect(userFuncObj.getClass().getMethod("doFunction", Map.class));
            if (handler != null) {
                ServerlessParameter parameter = new ServerlessParameter();
                parameter.setParamName("map");
                parameter.setSignature(new TypeSignature() {
                    @Override
                    public boolean equalsIgnoringTypeParams(TypeSignature typeSignature) {
                        return false;
                    }

                    @Override
                    protected String toStringInternal(boolean b) {
                        return "java.util.Map";
                    }

                    @Override
                    protected String getClassName() {
                        return "java.util.Map";
                    }
                });
                List<ServerlessParameter> parameters = new ArrayList<>();
                parameters.add(parameter);
                serverlessFunMap.put(functionName, Triple.of(handler, parameters, false));
            }
        }
    }

    public boolean hasFunction(String functionName) {
        return serverlessFunMap.containsKey(functionName);
    }

    public Object invokeFunc(HttpServletRequest request, HttpServletResponse response, String funcName) {
        Object retObj ;
        if (serverlessFunMap.containsKey(funcName)) {
            Triple<MethodHandle, List<ServerlessParameter>, Boolean> function = serverlessFunMap.get(funcName);
            Map<String, String> paramMap = new HashMap<>();
            wrapRequestParameter(request, paramMap);
            try {
                if (!CollectionUtils.isEmpty(function.getMiddle())) {
                    List<Object> objects = new ArrayList<>();
                    String content = null;
                    if (request.getMethod().equalsIgnoreCase("post") || request.getMethod().equalsIgnoreCase("put")) {
                        StringBuilder builder = new StringBuilder();
                        readContent(request, builder);
                        content = builder.toString();
                        Assert.isTrue(content.startsWith("{") && content.endsWith("}"), "content not a validate json");
                    }

                    for (ServerlessParameter parameter : function.getMiddle()) {
                        if (parameter.getSignature().toString().contains("HttpServletRequest")) {
                            objects.add(request);
                        } else if (parameter.getSignature().toString().contains("HttpServletResponse")) {
                            objects.add(response);
                        }
                        if (parameter.getSignature().toString().startsWith("java.util.Map")) {
                            if (!ObjectUtils.isEmpty(content)) {
                                Map<String, Object> reqMap = gson.fromJson(content, new TypeToken<Map<String, Object>>() {
                                }.getType());
                                objects.add(reqMap);
                            } else {
                                throw new MissingConfigException("content is null,please use post or put to access HashMap");
                            }
                        } else if (paramMap.containsKey(parameter.getParamName())) {
                            objects.add(getValue(parameter.getSignature(), paramMap.get(parameter.getParamName())));
                        }

                    }
                    if (function.getRight()) {
                        retObj = function.getLeft().invokeWithArguments(objects);
                    } else {
                        Object obj = loadBeanOrUserDefined(funcName);
                        retObj = function.getLeft().bindTo(obj).invokeWithArguments(objects);
                    }
                } else {
                    if (function.getRight()) {
                        retObj = function.getLeft().invoke(null);
                    } else {
                        Object obj = loadBeanOrUserDefined(funcName);
                        retObj = function.getLeft().bindTo(obj).invokeExact(null);
                    }
                }
                return retObj;
            } catch (Throwable ex) {
                return AbstractController.wrapFailedMsg(ex.getMessage());
            }
        } else {
            return AbstractController.wrapFailedMsg("function name " + funcName + " does not register!");
        }
    }

    private static void readContent(HttpServletRequest request, StringBuilder builder) {
        try (BufferedReader reader = request.getReader()) {
            String lineStr ;
            while ((lineStr = reader.readLine()) != null) {
                builder.append(lineStr);
            }
        } catch (IOException ex1) {
            throw new MissingConfigException(ex1);
        }
    }

    private Object loadBeanOrUserDefined(String funcName) {
        Object obj = SpringContextHolder.getBean(functionOriginClassMap.get(funcName));
        if (obj == null) {
            if (userDefinedObjectMap.containsKey(funcName)) {
                obj = userDefinedObjectMap.get(funcName);
            } else {
                throw new MissingConfigException("with exception of static function and user defined,object must be managed bean");
            }
        }
        return obj;
    }

    public void wrapRequestParameter(HttpServletRequest request, Map<String, String> paramMap) {
        Map<String, String[]> reqMap = request.getParameterMap();
        if (!CollectionUtils.isEmpty(reqMap)) {
            reqMap.entrySet().forEach(entry -> paramMap.put(entry.getKey(), entry.getValue()[0]));
        }
    }

    public Object getValue(TypeSignature signature, String value) throws Throwable {
        prototypeHandlerMap.computeIfAbsent(signature.toString(), this::getHandler);
        if (prototypeHandlerMap.get(signature.toString()) != null) {
            return prototypeHandlerMap.get(signature.toString()).invoke(value);
        } else {
            throw new MissingConfigException("valueOf method not found in paramter " + signature);
        }

    }

    private MethodHandle getHandler(String clazzName) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getMethod("valueOf", String.class);
            return MethodHandles.publicLookup().unreflect(method);
        } catch (Exception ex) {
            throw new MissingConfigException("method valueOf missing!");
        }
    }
}
