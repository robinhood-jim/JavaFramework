package com.robin.tracer.utils;

import brave.Span;
import brave.Tracing;
import com.robin.tracer.plugin.datasource.AbstractSpringAwareJdbcOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.sql.SQLException;

@Slf4j
public class TracingUtils {


    public static final Span recordChildSpanStart(Tracing tracing,Span parentSpan,String displayName,String key){
        tracing.tracer().startScopedSpanWithParent(displayName, parentSpan.context());
        Span chindSpan = tracing.tracer().currentSpan();
        chindSpan.kind(Span.Kind.CONSUMER);
        if(key!=null && !key.isEmpty()) {
            chindSpan.tag("method", key);
        }
        return chindSpan;
    }
    public static final Span recordSpanWithCurrent(Tracing tracing,String serviceName, String key,String ip,int port, ConstructSpan constructSpan){
        Span span=null;
        if(tracing!=null) {
            if (tracing.tracer().currentSpan() != null) {
                span = tracing.tracer().newChild(tracing.tracer().currentSpan().context());
            } else {
                span = tracing.tracer().newTrace();
            }
            Assert.notNull(span,"");
            if (span != null) {
                span.kind(Span.Kind.CLIENT);
                span.remoteServiceName(serviceName);
                span.remoteIpAndPort(ip,port == 0 ? 3306 : port);
                span.name(key);
                if(constructSpan!=null) {
                    constructSpan.construct(span);
                }
            }
        }
        return span;
    }
    public static Object executeSqlOperationWithSpan(AbstractSpringAwareJdbcOperation operation, String name, String sql, ConstructSpan constructSpan, DoInSpan doInSpan, Object... params) throws SQLException{
        Span span=null;
        try {
            if(operation.getParam()!=null && operation.getParam().isEnableTracer()) {
                if (sql != null && !StringUtils.isEmpty(sql)) {
                    operation.setExecuteSql(sql);
                }
                if(isSqlRecordable(operation.getExecuteSql())) {
                    String serviceName = operation.getParam().getDbType().toLowerCase() + "-" + operation.getParam().getHost();
                    span = TracingUtils.recordSpanWithCurrent(operation.getTracing(), serviceName, name, operation.getParam().getHost(),
                            operation.getParam().getPort(), constructSpan);
                    Assert.notNull(span,"");
                    span.start();
                }
                Object rs = doInSpan.callMethod(params);
                if(span!=null) {
                    span.finish();
                }
                return rs;
            }
            return doInSpan.callMethod(params);
        }catch (SQLException ex){
            if(span!=null) {
                span.error(ex);
            }
            throw ex;
        }
    }

    private static boolean isSqlRecordable(String sql){
        return !("select 1".equalsIgnoreCase(sql) || "select 1 from dual".equalsIgnoreCase(sql));
    }
    @FunctionalInterface
    public interface  ConstructSpan{
        void construct(Span span);
    }
    @FunctionalInterface
    public interface DoInSpan{
        Object callMethod(Object... params) throws SQLException;
    }
}
