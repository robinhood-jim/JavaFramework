package com.robin.agent;

import java.lang.instrument.Instrumentation;

public class CustomAgent {
    public static void premain(String args, Instrumentation instrumentation){
        instrumentation.addTransformer(new ClassTransformer());
    }
}
