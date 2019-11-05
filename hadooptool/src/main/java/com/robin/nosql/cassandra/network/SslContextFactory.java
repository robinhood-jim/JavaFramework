package com.robin.nosql.cassandra.network;

import com.robin.core.base.exception.MissingConfigException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;


public class SslContextFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SslContextFactory.class);

    private SslContextFactory() {
    }

    public static SslContext createSslContext(String sslConfigPath) throws GeneralSecurityException, IOException {
        if (sslConfigPath == null) {
            throw new MissingConfigException("Please specify SSL config path in cdc.yml");
        } else {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(sslConfigPath);
            Throwable ex = null;

            SslContext sslContext;
            try {
                props.load(fis);
                fis.close();
                SslConfig sslConfig = new SslConfig(props);
                sslContext = createSslContext(sslConfig);
            } catch (Throwable var14) {
                ex = var14;
                throw var14;
            } finally {
                if (fis != null) {
                    if (ex != null) {
                        try {
                            fis.close();
                        } catch (Throwable var13) {
                            ex.addSuppressed(var13);
                        }
                    } else {
                        fis.close();
                    }
                }

            }

            return sslContext;
        }
    }

    public static SslContext createSslContext(SslConfig config) throws GeneralSecurityException, IOException {
        try {
            SslContextBuilder builder = SslContextBuilder.forClient();
            KeyStore trustStore;
            FileInputStream is;
            Throwable ex;
            if (config.keyStoreLocation() != null) {
                trustStore = KeyStore.getInstance(config.keyStoreType());

                try {
                    is = new FileInputStream(config.keyStoreLocation());
                    ex = null;

                    try {
                        trustStore.load(is, config.keyStorePassword().toCharArray());
                    } catch (Throwable var34) {
                        ex = var34;
                        throw var34;
                    } finally {
                        if (is != null) {
                            if (ex != null) {
                                try {
                                    is.close();
                                } catch (Throwable var31) {
                                    ex.addSuppressed(var31);
                                }
                            } else {
                                is.close();
                            }
                        }

                    }
                } catch (IOException var36) {
                    LOGGER.error("failed to load the key store: location=" + config.keyStoreLocation() + " type=" + config.keyStoreType());
                    throw var36;
                }

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(config.getKeyManagerAlgorithm());
                keyManagerFactory.init(trustStore, config.keyStorePassword().toCharArray());
                builder = SslContextBuilder.forClient();
                builder.keyManager(keyManagerFactory);
            } else {
                LOGGER.error("KeyStoreLocation was not specified. Building SslContext without certificate. This is not suitable for PRODUCTION");
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                builder = builder.keyManager(ssc.certificate(), ssc.privateKey());
            }

            if (config.trustStoreLocation() != null) {
                trustStore = KeyStore.getInstance(config.trustStoreType());

                try {
                    is = new FileInputStream(config.trustStoreLocation());
                    ex = null;

                    try {
                        trustStore.load(is, config.trustStorePassword().toCharArray());
                    } catch (Throwable var33) {
                        ex = var33;
                        throw var33;
                    } finally {
                        if (is != null) {
                            if (ex != null) {
                                try {
                                    is.close();
                                } catch (Throwable var32) {
                                    ex.addSuppressed(var32);
                                }
                            } else {
                                is.close();
                            }
                        }

                    }
                } catch (IOException var38) {
                    LOGGER.error("failed to load the trust store: location=" + config.trustStoreLocation() + " type=" + config.trustStoreType());
                    throw var38;
                }

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(config.trustManagerAlgorithm());
                trustManagerFactory.init(trustStore);
                builder.trustManager(trustManagerFactory);
            } else {
                LOGGER.error("TrustStoreLocation was not specified. Building SslContext using InsecureTrustManagerFactory. This is not suitable for PRODUCTION");
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            }

            return builder.build();
        } catch (IOException | GeneralSecurityException ex1) {
            LOGGER.error("Failed to create SslContext", ex1);
            throw ex1;
        }
    }
}
