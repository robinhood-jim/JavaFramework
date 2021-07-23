package com.robin.tracer.plugin.datasource;

import brave.Tracing;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import com.robin.tracer.utils.TracerConstant;

public class ProxyDataSource implements DataSource {
    private String appName;

    private Tracing tracing;
    private DataSource delegate;
    private boolean traceEnable;

    private TraceParam traceParam;
    public ProxyDataSource(DataSource dataSource, Environment environment,Tracing tracing){
        this.delegate=dataSource;
        this.appName=environment.getProperty(TracerConstant.APPNAME_KEY);
        this.traceEnable=environment.containsProperty(TracerConstant.ENABLE_DATASOURCETRACE+".enabled") && "true".equalsIgnoreCase(environment.getProperty(TracerConstant.ENABLE_DATASOURCETRACE+".enabled"));
        this.tracing=tracing;
        traceParam=new TraceParam(appName,traceEnable);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ProxyConnection(delegate.getConnection(),tracing,traceParam);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ProxyConnection(delegate.getConnection(username,password),tracing,traceParam);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    public void setTracing(Tracing tracing) {
        this.tracing = tracing;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


    public Tracing getTracing() {
        return tracing;
    }

    public DataSource getDelegate() {
        return delegate;
    }

    public void setDelegate(DataSource delegate) {
        this.delegate = delegate;
    }

    public boolean isTraceEnable() {
        return traceEnable;
    }

    public void setTraceEnable(boolean traceEnable) {
        this.traceEnable = traceEnable;
    }
}
