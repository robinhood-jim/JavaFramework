package com.robin.spring;
import  org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.LaunchedURLClassLoader;

import java.net.URL;

public class CustomJarLauncher extends JarLauncher{
    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        CustomerURLClassLoader loader=new CustomerURLClassLoader(urls,this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        return new LaunchedURLClassLoader(true, this.getArchive(), urls, loader);
    }
    public static void main(String[] args) throws Exception {
        new CustomJarLauncher().launch(args);
    }

}
