package com.robin.nosql.cassandra;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class CassandraPressitUtil {
    private String clusterNames;
    private String userName;
    private String password;
    private Cluster cluster;
    private Session session;

    private PreparedStatement statement;
    private BoundStatement boundStatement;
    private BatchStatement batchStatement;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String keySpace;

    public CassandraPressitUtil(String clusterNames, String userName, String password, String sql) {
        this.clusterNames = clusterNames;
        this.userName = userName;
        this.password = password;
        getSession();
        statement = session.prepare(sql);
        boundStatement = new BoundStatement(statement);
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
    }

    public void checkSession() {
        if (session == null || session.isClosed()) {
            getSession();
        }
    }

    public CassandraPressitUtil(String clusterNames, String userName, String password, String sql, String keySpace) {
        this.clusterNames = clusterNames;
        this.userName = userName;
        this.password = password;
        this.keySpace = keySpace;
        getSession();
        statement = session.prepare(sql);
        boundStatement = new BoundStatement(statement);
        batchStatement = new BatchStatement();
    }


    private void getSession() {
        Cluster.Builder builder = Cluster.builder().withoutJMXReporting().addContactPoints(clusterNames.split(","));
        builder.getConfiguration().getCodecRegistry().register(TypeCodec.timeUUID());
        if (userName != null && password != null && !userName.isEmpty() && !password.isEmpty()) {
            builder = builder.withAuthProvider(new PlainTextAuthProvider(userName, password));
        }
        cluster = builder.build();
        if (keySpace != null) {
            session = cluster.connect(keySpace);
        } else {
            session = cluster.connect();
        }
    }

    public void addRecordWithBatchSize(Object[] objects, int batchSize) {
        if (batchStatement.size() >= batchSize) {
            batchInsert();
            batchStatement.clear();
        }
        batchStatement.add(boundStatement.bind(objects));
    }

    public void addRecordWithBatchSize(List<Object[]> objects, int batchSize) {
        batchStatement.add(boundStatement.bind(objects));
        if (batchStatement.size() >= batchSize) {
            batchInsert();
            batchStatement.clear();
        }
    }

    public void addBatchRecord(Object[] objects) {
        BoundStatement stmt = new BoundStatement(statement);
        stmt.bind(objects);
        batchStatement.add(stmt);
    }

    public void addBatchRecord(Map<String, Object> valueMap, String[] insertColumns) {
        Object[] objs = new Object[insertColumns.length];
        for (int i = 0; i < insertColumns.length; i++) {
            String insertColumn = insertColumns[i];
            if (valueMap.containsKey(insertColumn)) {
                objs[i] = valueMap.get(insertColumn);
            } else {
                objs[i] = null;
            }
        }
        batchStatement.add(boundStatement.bind(objs));
    }

    public void alertMsg() {
        if (batchStatement.size() > 0) {
            logger.info("-- flush record {}", batchStatement.size());
        }
    }

    public void flushRecords() {
        batchInsert();
        batchStatement.clear();
    }

    public void flushRemains() {
        if (session != null && session.isClosed() && batchStatement.size() > 0) {
            batchInsert();
        }
    }

    public void addRecord(Object[] objects) {
        checkSession();
        session.execute(boundStatement.bind(objects));
    }

    public void batchInsert() {
        checkSession();
        if (batchStatement.size() > 0) {
            session.execute(batchStatement);
        }
    }

    public void close() {
        try {
            session.close();
            session.getCluster().close();
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

    }

    public static void main(String[] args) {
        try {
            //test with thingsboard insert
            CassandraPressitUtil util = new CassandraPressitUtil("cassandra1.cloud.123cx.com,cassandra2.cloud.123cx.com,cassandra3.cloud.123cx.com", "cassandra", "123456", "insert into ts_kv_cf(entity_type,entity_id,key,partition,ts,str_v) values (?,?,?,?,?,?)", "testtab");
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2cb12c10-0bcd-11e8-83a1-ed1998e91527"), "LAT", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2cb12c10-0bcd-11e8-83a1-ed1998e91527"), "LON", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2cb12c10-0bcd-11e8-83a1-ed1998e91527"), "SPEED", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2cb12c10-0bcd-11e8-83a1-ed1998e91527"), "DIRECTION", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2d468e40-0bcd-11e8-83a1-ed1998e91527"), "LAT", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2d468e40-0bcd-11e8-83a1-ed1998e91527"), "LON", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2d468e40-0bcd-11e8-83a1-ed1998e91527"), "SPEED", 15551231230L, 15551231230L, "2222"});
            util.addBatchRecord(new Object[]{"DEVICE", UUID.fromString("2d468e40-0bcd-11e8-83a1-ed1998e91527"), "DIRECTION", 15551231230L, 15551231230L, "2222"});
            util.batchInsert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
