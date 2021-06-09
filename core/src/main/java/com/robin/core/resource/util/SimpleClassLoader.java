package com.robin.core.resource.util;

/*
 * SimpleClassLoader.java - a bare bones class loader.
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Hashtable;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

@Slf4j
public class SimpleClassLoader extends ClassLoader {
    private Hashtable classes = new Hashtable();

    public SimpleClassLoader() {
    }

    /**
     * This sample function for reading class implementations reads
     * them from the local file system
     */
    private byte[] getClassImplFromDataBase(String className) {
        if(log.isDebugEnabled()) {
            log.debug("        >>>>>> Fetching the implementation of " + className);
        }
    	byte[] result;
    	try(FileInputStream fi = new FileInputStream("store\\"+className+".impl")) {
    	    result = new byte[fi.available()];
    	    fi.read(result);
    	    fi.close();
    	    return result;
    	}catch (IOException ex){
    	    return null;
        }
    }

    /**
     * This is a simple version for external clients since they
     * will always want the class resolved before it is returned
     * to them.
     */
    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * This is the required version of loadClass which is called
     * both from loadClass above and from the internal function
     * FindClassFromClass.
     */
    @Override
    public synchronized Class loadClass(String className, boolean resolveIt)
    	throws ClassNotFoundException {
        Class result;
        byte[]  classData;

        System.out.println("        >>>>>> Load class : "+className);

        /* Check our local cache of classes */
        result = (Class)classes.get(className);
        if (result != null) {
            System.out.println("        >>>>>> returning cached result.");
            return result;
        }

        /* Check with the primordial class loader */
        try {
            result = super.findSystemClass(className);
            System.out.println("        >>>>>> returning system class (in CLASSPATH).");
            return result;
        } catch (ClassNotFoundException e) {
            System.out.println("        >>>>>> Not a system class.");
        }

        /* Try to load it from our repository */
        classData = getClassImplFromDataBase(className);
        if (classData == null) {
            throw new ClassNotFoundException();
        }

        /* Define it (parse the class file) */
        result = defineClass(null,classData, 0, classData.length);
        if (result == null) {
            throw new ClassFormatError();
        }

        if (resolveIt) {
            resolveClass(result);
        }

        classes.put(className, result);
        System.out.println("        >>>>>> Returning newly loaded class.");
        return result;
    }
}