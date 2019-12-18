package com.robin.nosql.cassandra.network;

import java.util.Properties;


public class SslConfig {
    public static final String KEY_STORE_LOCATION = "keyStore.location";
    public static final String KEY_STORE_PASSWORD = "keyStore.password";
    public static final String KEY_STORE_TYPE = "keyStore.type";
    public static final String DEFAULT_KEY_STORE_TYPE = "JKS";
    public static final String TRUST_STORE_LOCATION = "trustStore.location";
    public static final String TRUST_STORE_PASSWORD = "trustStore.password";
    public static final String TRUST_STORE_TYPE = "trustStore.type";
    public static final String DEFAULT_TRUST_STORE_TYPE = "JKS";
    public static final String KEY_MANAGER_ALGORITHM = "keyManager.algorithm";
    public static final String DEFAULT_KEY_MANAGER_ALGORITHM = "SunX509";
    public static final String TRUST_MANAGER_ALGORITHM = "trustManager.algorithm";
    public static final String DEFAULT_TRUST_MANAGER_ALGORITHM = "SunX509";
    private Properties configs;

    public SslConfig(Properties configs) {
        this.configs = configs;
    }

    public String keyStoreLocation() {
        return (String)this.configs.get("keyStore.location");
    }

    public String keyStorePassword() {
        return (String)this.configs.get("keyStore.password");
    }

    public String keyStoreType() {
        return (String)this.configs.getOrDefault("keyStore.type", "JKS");
    }

    public String getKeyManagerAlgorithm() {
        return (String)this.configs.getOrDefault("keyManager.algorithm", "SunX509");
    }

    public String trustStoreLocation() {
        return (String)this.configs.get("trustStore.location");
    }

    public String trustStorePassword() {
        return (String)this.configs.get("trustStore.password");
    }

    public String trustStoreType() {
        return (String)this.configs.getOrDefault("trustStore.type", "JKS");
    }

    public String trustManagerAlgorithm() {
        return (String)this.configs.getOrDefault("trustManager.algorithm", "SunX509");
    }
}
