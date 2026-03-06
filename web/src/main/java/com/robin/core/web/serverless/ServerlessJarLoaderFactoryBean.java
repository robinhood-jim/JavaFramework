package com.robin.core.web.serverless;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.web.controller.AbstractController;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServerlessJarLoaderFactoryBean implements InitializingBean {
    //default classloader carry infrastructure
    private ClassLoader defaultLoader = null;
    private final DataCollectionMeta defaultMetaDefined = new DataCollectionMeta();
    private final Map<String, DynamicJarClassLoader> classLoaderMap = new HashMap<>();
    private String defaultScanPackage = "com.robin";
    private final Map<String, DynamicFunction> serverlessFunMap = new HashMap<>();
    private final Map<String, MethodHandle> prototypeHandlerMap = new HashMap<>();
    private final Map<String, Class<?>> functionOriginClassMap = new HashMap<>();
    private final Map<String, Object> userDefinedObjectMap = new HashMap<>();
    private final Map<String, Pair<String, MethodHandle>> initFuncMap = new HashMap<>();
    private final Map<String, List<String>> jarFunctionListMap = new HashMap<>();
    private final Gson gson = GsonUtil.getGson();
    private String basePath = "/data/runningLibs/jars/";
    private final DataCollectionMeta collectionMeta;
    private final AbstractFileSystemAccessor accessor;

    public  ServerlessJarLoaderFactoryBean(DataCollectionMeta collectionMeta, AbstractFileSystemAccessor accessor) {
        this.collectionMeta = collectionMeta;
        this.accessor = accessor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Environment environment = SpringContextHolder.getBean(Environment.class);
        //default jars must be local
        defaultMetaDefined.setFsType(Const.FILESYSTEM.LOCAL.getValue());
        if (environment != null && !ObjectUtils.isEmpty(environment.getProperty("project.serverless.defaultJars"))) {
            //default load local file jar,no need to dynamic update
            String[] loadJar = environment.getProperty("project.serverless.defaultJars").split(",");
            URL[] urls = new URL[loadJar.length];
            for (int i = 0; i < loadJar.length; i++) {
                urls[i] = new File(loadJar[i]).toURI().toURL();
            }
            defaultLoader = new URLClassLoader(urls, getClass().getClassLoader());
        } else {
            defaultLoader = getClass().getClassLoader();
        }
        if (environment != null && environment.containsProperty("project.serverless.basePackage")) {
            defaultScanPackage = environment.getProperty("project.serverless.basePackage");
        }
        if (environment != null && environment.containsProperty("project.serverless.jarDefaultPath")) {
            basePath = environment.getProperty("project.serverless.jarDefaultPath");
        }
    }

    //dynamic add a new URLClassLoader to access
    public void addJar(String jarFile, String scanPackage) throws IOException {
        DynamicJarClassLoader classLoader = new DynamicJarClassLoader(new URL[]{}, defaultLoader, collectionMeta, basePath + jarFile, accessor);
        List<String> funcList = new ArrayList<>();
        FunctionScanner.scanPackageOrigin(ObjectUtils.isEmpty(scanPackage) ? defaultScanPackage : scanPackage, classLoader, serverlessFunMap, functionOriginClassMap, userDefinedObjectMap, initFuncMap, funcList);
        //FunctionScanner.scanPackage(ObjectUtils.isEmpty(scanPackage)?defaultScanPackage:scanPackage,classLoader,serverlessFunMap,functionOriginClassMap,userDefinedObjectMap,initFuncMap,funcList);
        String jarFileName = getJarName(jarFile);
        jarFunctionListMap.put(jarFileName, funcList);
        classLoaderMap.put(jarFileName, classLoader);
    }

    //unload dynamic jar
    public void removeJar(String jarFile) throws IOException {
        String jarFileName = getJarName(jarFile);
        if (!ObjectUtils.isEmpty(jarFileName)) {
            if (classLoaderMap.containsKey(jarFileName)) {
                DynamicJarClassLoader loader = classLoaderMap.remove(jarFileName);
                if (jarFunctionListMap.containsKey(jarFileName)) {
                    jarFunctionListMap.get(jarFileName).forEach(f -> {
                        DynamicFunction function=serverlessFunMap.remove(f);
                        function.setInvokeMethod(null);
                        function=null;
                        functionOriginClassMap.remove(f);
                        if (userDefinedObjectMap.containsKey(f)) {
                            //remove no static function call object
                            Object obj = userDefinedObjectMap.remove(f);
                            obj = null;
                        }
                        initFuncMap.remove(f);
                    });
                    jarFunctionListMap.remove(jarFileName);
                }
                loader.close();
                System.gc();
            }
        }
    }

    public void updateJar(String jarFile, String scanPackage) throws IOException {
        removeJar(jarFile);
        addJar(jarFile, scanPackage);
    }

    public void registerServerlessFunction(String functionName, String callMethod, IUserDefineServerlessFunction serverlessFunction) throws NoSuchMethodException, IllegalAccessException {
        if (serverlessFunMap.containsKey(functionName)) {
            throw new MissingConfigException("function " + functionName + " already defined!");
        } else {
            IUserDefineServerlessFunction userFuncObj = (IUserDefineServerlessFunction) Proxy.newProxyInstance(defaultLoader, new Class[]{IUserDefineServerlessFunction.class},
                    (proxy, method, args) -> serverlessFunction.doFunction((Map<String, Object>) args[0]));
            userDefinedObjectMap.put(functionName, userFuncObj);
            MethodHandle handler = MethodHandles.publicLookup().unreflect(userFuncObj.getClass().getMethod("doFunction", Map.class));
            if (handler != null) {
                ServerlessParameter parameter = new ServerlessParameter();
                parameter.setParamName("map");
                parameter.setJavaType(Map.class);
                List<ServerlessParameter> parameters = new ArrayList<>();
                parameters.add(parameter);
                serverlessFunMap.put(functionName, new DynamicFunction(handler, parameters, false, callMethod));
            }
        }
    }
    public void registerServerlessFunctionByCode(String functionName,Object callMethods,Object className,String javaCode) throws Exception{
        ClassPool pool=ClassPool.getDefault();
        String classPartName=ObjectUtils.isEmpty(className)?"Proxy$"+System.currentTimeMillis():className.toString();
        String classFullName="com.robin.serverless.function."+classPartName;
        CtClass ctClass=pool.makeClass(classFullName);
        pool.insertClassPath(new ClassClassPath(IUserDefineServerlessFunction.class));
        CtClass interfaceClazz=pool.get(IUserDefineServerlessFunction.class.getName());
        ctClass.addInterface(interfaceClazz);
        CtMethod method=new CtMethod(pool.get("java.lang.Object"),"doFunction",new CtClass[]{pool.get("java.util.Map")},ctClass);
        String content=javaCode.replace("map","$1");
        method.setBody("{"+content+"}");
        ctClass.addMethod(method);
        Class<?> dynamicClass=ctClass.toClass();
        Object userFuncObj=dynamicClass.getConstructor().newInstance();
        userDefinedObjectMap.put(functionName, userFuncObj);
        MethodHandle handler = MethodHandles.publicLookup().unreflect(dynamicClass.getMethod("doFunction", Map.class));
        if (handler != null) {
            ServerlessParameter parameter = new ServerlessParameter();
            parameter.setParamName("map");
            parameter.setJavaType(Map.class);
            List<ServerlessParameter> parameters = new ArrayList<>();
            parameters.add(parameter);
            serverlessFunMap.put(functionName, new DynamicFunction(handler, parameters, false, callMethods));
        }

    }

    public Object invokeFunc(HttpServletRequest request, HttpServletResponse response, String funcName) {
        Object retObj;
        if (serverlessFunMap.containsKey(funcName)) {
            DynamicFunction function = serverlessFunMap.get(funcName);
            if (!function.checkCallMethod(request.getMethod())) {
                return AbstractController.wrapFailedMsg("http method " + request.getMethod() + " not allowed!");
            }
            Map<String, String> paramMap = new HashMap<>();
            FunctionScanner.wrapRequestParameter(request, paramMap);
            try {
                if (!CollectionUtils.isEmpty(function.getParameters())) {
                    List<Object> objects = new ArrayList<>();
                    String content = null;
                    if (request.getMethod().equalsIgnoreCase("post") || request.getMethod().equalsIgnoreCase("put")) {
                        StringBuilder builder = new StringBuilder();
                        FunctionScanner.readContent(request, builder);
                        content = builder.toString();
                        Assert.isTrue(content.startsWith("{") && content.endsWith("}"), "content not a validate json");
                    }

                    for (ServerlessParameter parameter : function.getParameters()) {
                        if ((!ObjectUtils.isEmpty(parameter.getSignature()) && parameter.getSignature().toString().contains("HttpServletRequest"))
                                || (!ObjectUtils.isEmpty(parameter.getJavaType()) && parameter.getJavaType().getTypeName().contains("HttpServletRequest"))) {
                            objects.add(request);
                        } else if ((!ObjectUtils.isEmpty(parameter.getSignature()) && parameter.getSignature().toString().contains("HttpServletResponse"))
                                || (!ObjectUtils.isEmpty(parameter.getJavaType()) && parameter.getJavaType().getTypeName().contains("HttpServletResponse"))) {
                            objects.add(response);
                        }
                        if ((!ObjectUtils.isEmpty(parameter.getSignature()) && parameter.getSignature().toString().startsWith("java.util.Map"))
                                || (!ObjectUtils.isEmpty(parameter.getJavaType()) && parameter.getJavaType().getTypeName().contains("java.util.Map"))) {
                            if (!ObjectUtils.isEmpty(content)) {
                                Map<String, Object> reqMap = gson.fromJson(content, new TypeToken<Map<String, Object>>() {
                                }.getType());
                                objects.add(reqMap);
                            } else {
                                throw new MissingConfigException("content is null,please use post or put to access HashMap");
                            }
                        } else if (paramMap.containsKey(parameter.getParamName())) {
                            objects.add(FunctionScanner.getValue(parameter, paramMap.get(parameter.getParamName()), prototypeHandlerMap));
                        }

                    }
                    if (function.isFunStatic()) {
                        retObj = function.getInvokeMethod().invokeWithArguments(objects);
                    } else {
                        Object obj = FunctionScanner.loadBeanOrUserDefined(funcName, functionOriginClassMap, userDefinedObjectMap, initFuncMap);
                        retObj = function.getInvokeMethod().bindTo(obj).invokeWithArguments(objects);
                    }
                } else {
                    if (function.isFunStatic()) {
                        retObj = function.getInvokeMethod().invoke(null);
                    } else {
                        Object obj = FunctionScanner.loadBeanOrUserDefined(funcName, functionOriginClassMap, userDefinedObjectMap, initFuncMap);
                        retObj = function.getInvokeMethod().bindTo(obj).invoke(null);
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

    public Class<?> loadClass(String funcName) {
        if (functionOriginClassMap.containsKey(funcName) && serverlessFunMap.containsKey(funcName)) {
            return functionOriginClassMap.get(funcName);
        }
        return null;
    }

    private String getJarName(String jarPath) {

        int namePos = jarPath.indexOf(".");
        if (namePos != -1) {
            return jarPath.substring(0, namePos);
        }
        return null;

    }

    public boolean hasFunction(String functionName) {
        return serverlessFunMap.containsKey(functionName);
    }


}
