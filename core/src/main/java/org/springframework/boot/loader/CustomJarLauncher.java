package org.springframework.boot.loader;


import java.net.URL;

public class CustomJarLauncher extends JarLauncher{

    public CustomJarLauncher(){

    }
    public static void main(String[] args) throws Exception {
        (new CustomJarLauncher()).launch(args);
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        System.out.println("--begin to init ---");
        System.out.println(this.getClass().getClassLoader());
        CustomerClassLoader classLoader1=new CustomerClassLoader(urls,this.getClass().getClassLoader());
        return new LaunchedURLClassLoader(this.isExploded(), this.getArchive(),urls, classLoader1);
    }


}
