package com.robin.core.web.serverless;

import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

@Slf4j
public class DynamicJarClassLoader extends URLClassLoader {
    private final ClassLoader superloader;
    private final Map<String, byte[]> cachedMap = new ConcurrentHashMap<>();
    private final List<String> resources = new ArrayList<>();
    private final DataCollectionMeta collectionMeta;
    private static final String DOT = ".";
    private static final String SEP = "/";
    private URL callUrl = null;
    private String[] loadJars = null;
    private final AbstractFileSystemAccessor accessor;
    private final URLStreamHandler handler;

    public DynamicJarClassLoader(URL[] urls, ClassLoader parent, DataCollectionMeta collectionMeta, String loadJars, AbstractFileSystemAccessor accessor) throws MalformedURLException {
        super(urls, parent);
        superloader = parent;
        this.collectionMeta = collectionMeta;
        if (!ObjectUtils.isEmpty(loadJars)) {
            this.loadJars = loadJars.split(",");
        }
        handler = new FsStreamHandler();
        callUrl = new URL(collectionMeta.getFsType(), null, -1, SEP, handler);
        super.addURL(callUrl);
        this.accessor = accessor;
        try {
            doInit();
        } catch (IOException ex) {

        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        byte[] data = cachedMap.get(name);
        if (data == null) {
            return superloader.loadClass(name);
        }
        log.debug("dynamic load class " + name);
        Class<?> clazz = defineClass(translateName(name), data, 0, data.length);
        return clazz;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] data = cachedMap.get(name);
        if (data == null) {
            return superloader.getResourceAsStream(name);
        }
        return new ByteArrayInputStream(data);
    }

    @Override
    public URL findResource(String name) {
        try {
            if (cachedMap.containsKey(name)) {
                return new URL(collectionMeta.getFsType(), "localhost", 0, name, handler);
            } else {
                return super.findResource(name);
            }
        } catch (MalformedURLException ex) {

        }
        return null;
    }


    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();

        if (ObjectUtils.isEmpty(name)) {
            for (String resourceName : resources) {
                urls.add(new URL(collectionMeta.getFsType(), "localhost", 0, resourceName, handler));
            }
            return Collections.enumeration(urls);
        } else {
            List<String> filterList = resources.stream().filter(f -> f.startsWith(name)).collect(Collectors.toList());
            if (filterList.size() > 0) {
                for (String resourceName : filterList) {
                    urls.add(new URL(collectionMeta.getFsType(), "localhost", 0, resourceName, handler));
                }
                return Collections.enumeration(urls);
            } else {
                return super.findResources(name);
            }
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();

        if (ObjectUtils.isEmpty(name)) {
            for (String resourceName : resources) {
                urls.add(new URL(collectionMeta.getFsType(), "localhost", 0, resourceName, handler));
            }
            return Collections.enumeration(urls);
        } else {
            List<String> filterList = resources.stream().filter(f -> f.startsWith(name)).collect(Collectors.toList());
            if (filterList.size() > 0) {
                for (String resourceName : filterList) {
                    urls.add(new URL(collectionMeta.getFsType(), "localhost", 0, resourceName, handler));
                }
                return Collections.enumeration(urls);
            } else {
                return superloader.getResources(name);
            }
        }
    }


    @Override
    public void close() throws IOException {
        cachedMap.clear();
        super.close();
    }

    private class FsStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new FsURLConnection(u);
        }
    }

    private class FsURLConnection extends URLConnection {


        public FsURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {

        }

        @Override
        public InputStream getInputStream() throws IOException {
            String fileName = url.getFile();
            byte[] data = cachedMap.get(fileName);
            if (data == null) {
                throw new FileNotFoundException("class " + fileName + " not found ");
            }
            return new ByteArrayInputStream(data);
        }
    }

    public void doInit() throws IOException {

        if (loadJars != null && loadJars.length > 0) {
            for (int i = 0; i < loadJars.length; i++) {
                extractByteCode(loadJars[i]);
            }
        }
    }

    private void extractByteCode(String fileName) throws IOException {
        ByteArrayOutputStream out ;
        int len = 0;
        byte[] b = new byte[8192];
        log.debug("begin to load with fileSystem {} path {}",collectionMeta.getFsType(),fileName);
        try (InputStream inputStream = accessor.getRawInputStream(fileName);
             JarInputStream jis = new JarInputStream(inputStream)) {
            JarEntry jarEntry ;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                String name = SEP + jarEntry.getName();
                if (jarEntry.isDirectory()) {
                    log.debug("ignore path " + name);
                    continue;
                }
                resources.add(name);
                out = new ByteArrayOutputStream();

                while ((len = jis.read(b)) > 0) {
                    out.write(b, 0, len);
                }

                log.debug("Jar entry = " + name);
                cachedMap.put(name, out.toByteArray());
                out.close();
            }
        }
    }

    private String translateName(String path) {
        String className = path.substring(1).replace(SEP, DOT);
        int pos = className.indexOf("class");
        return className.substring(0, pos - 1);
    }


    public List<String> getResources() {
        return resources;
    }
}
