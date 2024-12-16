package com.robin.core.resaccess.iterator;

import com.robin.comm.dal.holder.db.DbConnectionHolder;
import com.robin.comm.dal.holder.db.JdbcResourceHolder;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.dao.CommJdbcUtil;
import com.robin.core.base.exception.GenericException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class JdbcResIterator extends AbstractResIterator {
    private JdbcResourceHolder holder;
    private DbConnectionHolder connectionHolder;
    private String querySql;
    private ResultSet rs;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private LobHandler lobHandler;

    public JdbcResIterator() {
        this.identifier= Const.ACCESSRESOURCE.JDBC.getValue();
        lobHandler=new DefaultLobHandler();
    }

    public JdbcResIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }

    @Retryable(maxAttempts = 10, backoff = @Backoff(delay = 60000))
    public void getConnection()  {
        if (holder == null) {
            holder = SpringContextHolder.getBean(ResourceAccessHolder.class).getPoolJdbcHolder(colmeta.getDbSourceId(), colmeta, null);
        }
        if (connectionHolder == null) {
            connectionHolder = SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(colmeta.getDbSourceId(), colmeta.getDbMeta(), null);
        }
    }



    @Override
    public void beforeProcess() {
        try {
            getConnection();
            Assert.notNull(colmeta.getResourceCfgMap().get("selectSql"),"");
            querySql = colmeta.getResourceCfgMap().get("selectSql").toString();
            Object[] objs = null;
            if (colmeta.getResourceCfgMap().containsKey("queryParams")) {
                objs = (Object[]) colmeta.getResourceCfgMap().get("queryParams");
            }
            if (!Objects.isNull(objs)) {
                QueryRunner qRunner = new QueryRunner();
                preparedStatement = connectionHolder.getConnection().prepareStatement(querySql);
                qRunner.fillStatement(preparedStatement, objs);
                rs = preparedStatement.executeQuery();
            } else {
                statement = connectionHolder.getConnection().createStatement();
                rs = statement.executeQuery(querySql);
            }
        } catch (SQLException ex) {

        }
    }

    @Override
    public void afterProcess() {

    }

    @Override
    public void close() throws IOException {
        DbUtils.closeQuietly(statement);
        DbUtils.closeQuietly(preparedStatement);
        connectionHolder.close();
    }

    @Override
    public boolean hasNext() {
        try {
            return rs.next();
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public Map<String, Object> next() {
        Map<String, Object> map = new HashMap<>();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            for (int i = 0; i < count; i++) {
                String columnName = rsmd.getColumnName(i + 1);
                map.put(columnName, CommJdbcUtil.getRecordValue(rsmd,rs,lobHandler,i));
            }
        }catch (SQLException ex){
            throw new GenericException(ex);
        }
        return map;
    }

    @Override
    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        throw new OperationNotSupportException("jdbc can not access format data file!");
    }
}
