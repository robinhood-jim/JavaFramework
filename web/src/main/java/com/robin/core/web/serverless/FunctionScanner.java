package com.robin.core.web.serverless;

import com.robin.core.base.annotation.ServerlessFunction;
import com.robin.core.base.annotation.ServerlessMethodParam;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.spring.SpringContextHolder;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class FunctionScanner {
    private FunctionScanner() {

    }

    static void scanPackageOrigin(String scanPackage,DynamicJarClassLoader loader, Map<String, DynamicFunction> serverlessFunMap, Map<String, Class<?>> functionOriginClassMap, Map<String, Object> userDefinedObjectMap, Map<String, Pair<String, MethodHandle>> initFuncMap, List<String> funcList) throws IOException {
        List<String> resources = loader.getResources();
        for (String className : resources) {
            if (!className.endsWith("class")) {
                continue;
            }
            String packageName=getPackage(className);
            if(!packageName.startsWith(scanPackage)){
                continue;
            }
            Class<?> clazz ;
            try {
                clazz=loader.loadClass(className);
            } catch (ClassNotFoundException ex) {
                throw new MissingConfigException(ex.getMessage());
            }
            if (clazz != null) {
                Method[] methods = clazz.getDeclaredMethods();
                if (methods != null && methods.length > 0) {
                    for (Method m : methods) {
                        if (m.isAnnotationPresent(ServerlessFunction.class)) {
                            ServerlessFunction function = m.getAnnotation(ServerlessFunction.class);
                            boolean isStatic = Modifier.isStatic(m.getModifiers());
                            String funName = ObjectUtils.isEmpty(function.value()) ? m.getName() : function.value();
                            if (serverlessFunMap.containsKey(funName)) {
                                throw new MissingConfigException("function name " + funName + "already defined!");
                            }
                            Parameter[] parameters = m.getParameters();
                            List<ServerlessParameter> methodParameterInfoList = new ArrayList<>();
                            if (parameters != null && parameters.length > 0) {
                                for (Parameter p : parameters) {
                                    ServerlessMethodParam paramAnnoInfo = p.getAnnotation(ServerlessMethodParam.class);
                                    ServerlessParameter parameter = new ServerlessParameter();
                                    parameter.setParamName(p.getName());
                                    parameter.setJavaType(p.getType());
                                    if (paramAnnoInfo != null && !ObjectUtils.isEmpty(paramAnnoInfo.value())) {
                                        parameter.setParamName(paramAnnoInfo.value());
                                    }
                                    methodParameterInfoList.add(parameter);
                                }
                                serverlessFunMap.put(funName, new DynamicFunction(getMethodHandle(m), methodParameterInfoList, isStatic,function.method()));
                            } else {
                                serverlessFunMap.put(funName, new DynamicFunction(getMethodHandle(m), null, isStatic,function.method()));
                            }
                            if (!isStatic && !ObjectUtils.isEmpty(function.initFunc())) {
                                String initFunc = function.initFunc();
                                String initParam = function.initParam();
                                try {
                                    if (clazz != null) {
                                        if (ObjectUtils.isEmpty(initFunc)) {
                                            initFunc = "init";
                                        }
                                        Method method = clazz.getMethod(initFunc, String.class);
                                        if (!ObjectUtils.isEmpty(initParam)) {
                                            initFuncMap.put(funName, Pair.of(initParam, getMethodHandle(method)));
                                        } else {
                                            initFuncMap.put(funName, Pair.of(null, getMethodHandle(method)));
                                        }
                                    }
                                } catch (Exception ex) {
                                    throw new MissingConfigException(ex);
                                }
                            }
                            funcList.add(funName);
                            functionOriginClassMap.put(funName, clazz);
                        }
                    }
                }
            }
        }

    }

    static void scanPackage(String scanPackage, ClassLoader loader, Map<String, Triple<MethodHandle, List<ServerlessParameter>, Boolean>> serverlessFunMap, Map<String, Class<?>> functionOriginClassMap, Map<String, Object> userDefinedObjectMap, Map<String, Pair<String, MethodHandle>> initFuncMap, List<String> funcList) {
        ClassGraph graph = new ClassGraph().addClassLoader(loader).enableAllInfo();
        if (!ObjectUtils.isEmpty(scanPackage)) {
            graph.whitelistPackages(scanPackage);
        }

        try (ScanResult scanResult=graph.scan()){
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
                        if (serverlessFunMap.containsKey(funName)) {
                            throw new MissingConfigException("function name " + funName + "already defined!");
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
                            //Object obj = loadBeanOrUserDefined(funName,functionOriginClassMap,userDefinedObjectMap);
                            Class<?> clazz = info.loadClass();
                            try {
                                if (clazz != null) {
                                    if (ObjectUtils.isEmpty(initFunc)) {
                                        initFunc = "init";
                                    }
                                    Method method = clazz.getMethod(initFunc, String.class);
                                    if (!ObjectUtils.isEmpty(initParam)) {
                                        initFuncMap.put(funName, Pair.of(initParam, getMethodHandle(method)));
                                    } else {
                                        initFuncMap.put(funName, Pair.of(null, getMethodHandle(method)));
                                    }
                                }
                            } catch (Exception ex) {
                                throw new MissingConfigException(ex);
                            }
                        }
                        funcList.add(funName);
                        functionOriginClassMap.put(funName, info.loadClass());
                    }
                }
            }
        } catch (Exception ex) {

        }
    }

    static Object loadBeanOrUserDefined(String funcName, Map<String, Class<?>> functionOriginClassMap, Map<String, Object> userDefinedObjectMap, Map<String, Pair<String, MethodHandle>> initFuncMap) throws Throwable {
        Object obj = SpringContextHolder.getBean(functionOriginClassMap.get(funcName));
        if (obj == null) {
            //user defined function or loaded function
            if (userDefinedObjectMap.containsKey(funcName)) {
                obj = userDefinedObjectMap.get(funcName);
            } else {
                ServerlessJarLoaderFactoryBean bean = SpringContextHolder.getBean(ServerlessJarLoaderFactoryBean.class);
                if (bean != null) {
                    Class<?> clazz = bean.loadClass(funcName);
                    Constructor<?> constructor = clazz.getConstructor();
                    obj = constructor.newInstance();
                    if (initFuncMap.containsKey(funcName)) {
                        MethodHandle handle = initFuncMap.get(funcName).getRight();
                        if (!ObjectUtils.isEmpty(initFuncMap.get(funcName).getKey())) {
                            handle.bindTo(obj).invoke(initFuncMap.get(funcName).getKey());
                        }
                    }
                    userDefinedObjectMap.put(funcName, obj);
                } else {
                    throw new MissingConfigException("Bean ServerlessJarLoaderFactoryBean not registered!");
                }
            }
        }
        return obj;
    }

    static Class<?> loadClass(String funcName) {
        ServerlessJarLoaderFactoryBean bean = SpringContextHolder.getBean(ServerlessJarLoaderFactoryBean.class);
        if (bean != null) {
            return bean.loadClass(funcName);
        } else {
            throw new MissingConfigException("Bean ServerlessJarLoaderFactoryBean not registered!");
        }
    }

    public static void wrapRequestParameter(HttpServletRequest request, Map<String, String> paramMap) {
        Map<String, String[]> reqMap = request.getParameterMap();
        if (!CollectionUtils.isEmpty(reqMap)) {
            reqMap.entrySet().forEach(entry -> paramMap.put(entry.getKey(), entry.getValue()[0]));
        }
    }

    public static void readContent(HttpServletRequest request, StringBuilder builder) {
        try (BufferedReader reader = request.getReader()) {
            String lineStr;
            while ((lineStr = reader.readLine()) != null) {
                builder.append(lineStr);
            }
        } catch (IOException ex1) {
            throw new MissingConfigException(ex1);
        }
    }


    public static Object getValue(ServerlessParameter param, String value, Map<String, MethodHandle> prototypeHandlerMap) throws Throwable {
        String signature=param.getSignature()==null?param.getJavaType().getTypeName():param.getSignature().toString();
        prototypeHandlerMap.computeIfAbsent(signature, FunctionScanner::getHandler);
        if (prototypeHandlerMap.get(signature) != null) {
            return prototypeHandlerMap.get(signature).invoke(value);
        } else {
            throw new MissingConfigException("valueOf method not found in paramter " + signature);
        }

    }

    private static MethodHandle getMethodHandle(MethodInfo method) {
        try {
            return MethodHandles.publicLookup().unreflect(method.loadClassAndGetMethod());
        } catch (IllegalAccessException ex) {
            log.error("{}", ex.getMessage());
            throw new MissingConfigException(ex.getMessage());
        }
    }

    private static MethodHandle getMethodHandle(Method method) {
        try {
            return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            log.error("{}", ex.getMessage());
            throw new MissingConfigException(ex.getMessage());
        }
    }

    private static MethodHandle getHandler(String clazzName) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getMethod("valueOf", String.class);
            return MethodHandles.publicLookup().unreflect(method);
        } catch (Exception ex) {
            throw new MissingConfigException("method valueOf missing!");
        }
    }
    private static String getPackage(String name){
        String packageName=name;
        int pos=name.indexOf("class");
        if(pos!=-1){
            int startPos=0;
            if(packageName.startsWith("/")){
                startPos=1;
            }
            packageName=packageName.substring(startPos,pos-1);
        }
        pos=packageName.lastIndexOf("/");
        return packageName.substring(0,pos).replace("/",".");
    }
}
