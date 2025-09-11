package com.robin.rapidexcel.utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public class XMLFactoryUtils {
    private static XMLInputFactory inputFactory=defaultInput();
    private static XMLOutputFactory outputFactory=defaultOutput();
    private static XMLInputFactory defaultInput(){
        XMLInputFactory factory  = new com.fasterxml.aalto.stax.InputFactoryImpl();
        // To prevent XML External Entity (XXE) attacks
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        return factory;
    }
    private static XMLOutputFactory defaultOutput(){
        XMLOutputFactory factory  = new com.fasterxml.aalto.stax.OutputFactoryImpl();
        return factory;
    }
    public static XMLInputFactory getDefaultInputFactory(){
        return inputFactory;
    }
    public static XMLOutputFactory getDefaultOutputFactory(){
        return outputFactory;
    }
}
