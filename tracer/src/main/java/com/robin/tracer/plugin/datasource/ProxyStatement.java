package com.robin.tracer.plugin.datasource;

import brave.Tracing;
import com.robin.tracer.utils.TracingUtils;

import java.sql.*;


public class ProxyStatement extends AbstractSpringAwareJdbcOperation implements Statement {
    private Statement delegate;
    public ProxyStatement(Statement delegate, Tracing tracing,TraceParam param){
        this.delegate=delegate;
        this.tracing=tracing;
        this.init(param);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return (ResultSet) TracingUtils.executeSqlOperationWithSpan(this,"executeQuery",sql,this::constructDefault,this::executeQueryDeletgate,sql);
    }
    private Object executeQueryDeletgate(Object... objects) throws SQLException {
        if(objects.length==1){
            return delegate.executeQuery(objects[0].toString());
        }
        throw new SQLException("unknown Operation");
    }
    private Object executeUpdateDelegate(Object... objects) throws SQLException{
        if(objects.length==1){
            return delegate.executeUpdate(objects[0].toString());
        }else if(objects.length==2){
            if(objects[1] instanceof Integer){
                return delegate.executeUpdate(objects[0].toString(),(int)objects[1]);
            }else if(objects[1] instanceof Integer[]){
                return delegate.executeUpdate(objects[0].toString(),(int[])objects[1]);
            }else if(objects[1] instanceof String[]){
                return delegate.executeUpdate(objects[0].toString(),(String[])objects[1]);
            }else{
                throw new SQLException("unknown operation");
            }
        }
        throw new SQLException("unknown operation");
    }
    private Object executeDelegate(Object... objects) throws SQLException{
        if(objects.length==1){
            return delegate.execute(objects[0].toString());
        }else if(objects.length==2) {
            if(objects[1] instanceof Integer){
                return delegate.execute(objects[0].toString(),(int) objects[1]);
            } else if (objects[1] instanceof Integer[]) {
                return delegate.execute(objects[0].toString(), (int[]) objects[1]);
            } else if (objects[1] instanceof String[]) {
                return delegate.execute(objects[0].toString(), (String[]) objects[1]);
            }
        }
        throw new SQLException("unknown operation");

    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return (Integer) TracingUtils.executeSqlOperationWithSpan(this,"executeUpdate",sql,this::constructDefault,this::executeUpdateDelegate,sql);
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return (Boolean) TracingUtils.executeSqlOperationWithSpan(this,"execute",sql,this::constructDefault,this::executeDelegate,sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        delegate.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return delegate.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
       return (Integer) TracingUtils.executeSqlOperationWithSpan(this,"executeUpdate",sql,this::constructDefault,this::executeUpdateDelegate,sql,autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return (Integer) TracingUtils.executeSqlOperationWithSpan(this,"executeUpdate",sql,this::constructDefault,this::executeUpdateDelegate,sql,columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return (Integer) TracingUtils.executeSqlOperationWithSpan(this,"executeUpdate",sql,this::constructDefault,this::executeUpdateDelegate,sql,columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return (Boolean) TracingUtils.executeSqlOperationWithSpan(this,"execute",sql,this::constructDefault,this::executeDelegate,sql,autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return (Boolean) TracingUtils.executeSqlOperationWithSpan(this,"execute",sql,this::constructDefault,this::executeDelegate,sql,columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return (Boolean) TracingUtils.executeSqlOperationWithSpan(this,"execute",sql,this::constructDefault,this::executeDelegate,sql,columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
