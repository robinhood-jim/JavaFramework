package com.robin.nosql.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.StringUtils;
import com.robin.nosql.cassandra.network.SslContextFactory;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuppressWarnings({"unused"})
public class CassandraUtils {
    private final String clusterNames;
    private final String userName;
    private final String password;
    private Cluster cluster;
    private Session session;
    private final String keySpace;
    private final String sslPath;
    private final Cache<String,Map<Integer,String>> cache;
    public CassandraUtils(String clusterNames,String userName,String password,String keySpace,Object... params){
        this.clusterNames=clusterNames;
        this.userName=userName;
        this.password=password;
        this.keySpace=keySpace;
        if(params.length>0){
            sslPath=params[0].toString();
        }else{
            sslPath=null;
        }
        getSession();
        cache= CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    }

    private void getSession(){
        Cluster.Builder builder = Cluster.builder().withoutJMXReporting().addContactPoints(clusterNames.split(","));
        if(userName!=null && password!=null) {
            builder = builder.withAuthProvider(new PlainTextAuthProvider(userName, password));
        }
        if(sslPath!=null && !StringUtils.isEmpty(sslPath)){
            try {
                SslContext context = SslContextFactory.createSslContext(sslPath);
                SSLOptions sslOptions = new RemoteEndpointAwareNettySSLOptions(context);
                builder.withSSL(sslOptions);
            }catch (Exception ex){
                log.error("{}",ex.getMessage());
            }
        }
        cluster = builder.build();
        session= cluster.connect(keySpace);
    }
    public static boolean insertRowWithMap(@NonNull Session session,String keyspace,String tableName, Map<String,Object> valueMap){
        return insertRow(session,keyspace,tableName,valueMap);
    }
    public boolean insertRowWithMap(String keyspace,String tableName, Map<String,Object> valueMap){
        return insertRow(session,keyspace,tableName,valueMap);
    }
    private static boolean insertRow(@NonNull Session session, String keyspace, String tableName, @NonNull Map<String,Object> valueMap){
        Iterator<Map.Entry<String,Object>> iter=valueMap.entrySet().iterator();
        List<String> keys=new ArrayList<>();
        List<Object> valueList=new ArrayList<>();
        while(iter.hasNext()){
            Map.Entry<String,Object> entry=iter.next();
            keys.add(entry.getKey());
            valueList.add(entry.getValue());
        }
        return session.execute(QueryBuilder.insertInto(keyspace,tableName).values(keys,valueList)).wasApplied();
    }

    public static PreparedStatement getPrepareStatement(@NonNull Session session, String sql){
        return session.prepare(sql);
    }
    public static boolean deleteRow(@NonNull Session session, String tableName, String condition, Object... params){
        return session.execute("delete from "+tableName+ " where "+condition,params).wasApplied();
    }

    public static boolean insertRowWithSql(@NonNull Session session,String sql,Object... param){
        return session.execute(sql,param).wasApplied();
    }
    public boolean insertRowWithSql(String sql,Object... param){
        return session.execute(sql,param).wasApplied();
    }
    public static boolean batchInsert(@NonNull Session session,String sql,List<Object[]> resultObj){
        PreparedStatement statement=session.prepare(sql);
        BatchStatement batchstmt=new BatchStatement();
        for(Object[] obj:resultObj){
            BoundStatement boundStatement=new BoundStatement(statement);
            batchstmt.add(boundStatement.bind(obj));
        }
        return session.execute(batchstmt).wasApplied();
    }
    public  boolean batchInsert(String sql,List<Object[]> resultObj){
        return batchInsert(session,sql,resultObj);
    }

    public static void bindValueByRowType(@NonNull BoundStatement statement,int pos,Object value){
        if(value!=null){
            if(Long.class.isAssignableFrom(value.getClass())){
                statement.setLong(pos,(Long)value);
            }else if(Integer.class.isAssignableFrom(value.getClass())){
                statement.setInt(pos,(Integer)value);
            }else if(Double.class.isAssignableFrom(value.getClass())){
                statement.setDouble(pos,(Double)value);
            }else if(Timestamp.class.isAssignableFrom(value.getClass())){
                statement.setTimestamp(pos,(Timestamp)value);
            }else if(String.class.isAssignableFrom(value.getClass())){
                statement.setString(pos,value.toString());
            }
        }else{
            statement.setToNull(pos);
        }

    }
    public void close() {
        if(cluster!=null){
            cluster.close();
        }
        if(session!=null){
            session.close();
        }
    }
    public ResultSet executeQuery(String sql,Object... params){
        if(!ObjectUtils.isEmpty(params)){
            PreparedStatement statement=session.prepare(sql);
            Statement bindStmt=statement.bind(params);
            return session.execute(bindStmt);
        }else{
            return session.execute(sql);
        }
    }


    /**
     *
     * @param sql
     * @param params
     * @param pageSize
     * @param pageNum
     * @return
     */
    public List<Row> queryByPage(String sql,Object[] params,int pageSize,int pageNum){
        PreparedStatement statement=session.prepare(sql);
        Statement bindstmt=statement.bind(params);
        bindstmt.setFetchSize(pageSize);
        int searchtimes=-1;
        int pagePos=0;
        boolean isEnd=false;
        if(cache.getIfPresent(sql)!=null){
            Map<Integer,String> map=cache.getIfPresent(sql);
            if(pageNum>0) {
                if (map.containsKey(pageNum)) {
                    bindstmt=bindstmt.setPagingState(PagingState.fromString(map.get(pageNum)));
                } else {
                    if (pageNum > 2) {
                        for (pagePos = pageNum - 2; pagePos >= 0; pagePos--) {
                            if (map.containsKey(pagePos)) {
                                bindstmt=bindstmt.setPagingState(PagingState.fromString(map.get(pagePos)));
                            }
                        }
                    }
                    searchtimes = pageSize - pagePos - 1;
                }
            }
        }else{
            if(pageNum>1) {
                searchtimes=pageNum-1;
            }
        }
        ResultSet rs=null;
        String savingPageState=null;
        if(searchtimes>=0) {
            for (int j = 0; j<=searchtimes;j++){
                if(null!=savingPageState){
                    bindstmt=bindstmt.setPagingState(PagingState.fromString(savingPageState));
                }
                rs=session.execute(bindstmt);
                PagingState state=rs.getExecutionInfo().getPagingState();
                if(null !=state){
                    savingPageState=rs.getExecutionInfo().getPagingState().toString();
                    if(null == cache.getIfPresent(sql)){
                        Map<Integer,String> map=new HashMap<>();
                        map.put(j,savingPageState);
                        cache.put(sql,map);
                    }else{
                        cache.getIfPresent(sql).put(j,savingPageState);
                    }
                }
                if(rs.isFullyFetched() && null== state){
                    if(isEnd){
                        return Collections.emptyList();
                    }else {
                        isEnd=true;
                    }
                }
            }
        }else{
            rs=session.execute(bindstmt);
        }
        List<Row> list=new ArrayList<>();
        Iterator<Row> iter=rs.iterator();
        while(iter.hasNext() && list.size()<pageSize){
            list.add(iter.next());
        }
        return list;
    }

    /**
     * Sync Data through two Cassandra Cluster
     * @param sourceClusterConfig  source cluster config
     * @param targetClusterConfig  target cluster config
     * @param selectSql         source cluster select sql
     * @param params
     * @param insertSql         target insert Sql
     * @param ttlTime if using ttl
     */
    public static void syncCassandra(@NonNull CassandraConfig sourceClusterConfig,CassandraConfig targetClusterConfig,String selectSql,Object[] params,String insertSql,long ttlTime){
        Cluster sourceCluster=null;
        Session sourceSession=null;
        Cluster targetCluster=null;
        Session targetSession=null;
        try {
            Cluster.Builder builder = Cluster.builder().withoutJMXReporting().addContactPoints(sourceClusterConfig.getClusterNames().split(","));
            if (sourceClusterConfig.getUserName() != null && sourceClusterConfig.getPassword() != null) {
                builder = builder.withAuthProvider(new PlainTextAuthProvider(sourceClusterConfig.getUserName(), sourceClusterConfig.getPassword()));
            }

            builder.withQueryOptions(new QueryOptions().setFetchSize(sourceClusterConfig.getFetchSize()));
            sourceCluster = builder.build();
            sourceSession = sourceCluster.connect(sourceClusterConfig.getKeySpace());


            Cluster.Builder targetbuilder = Cluster.builder().withoutJMXReporting().addContactPoints(targetClusterConfig.getClusterNames().split(","));
            if (targetClusterConfig.getUserName() != null && sourceClusterConfig.getPassword()!= null) {
                targetbuilder = targetbuilder.withAuthProvider(new PlainTextAuthProvider(targetClusterConfig.getUserName(), targetClusterConfig.getPassword()));
            }
            targetCluster = targetbuilder.build();
            targetSession = targetCluster.connect(targetClusterConfig.getKeySpace());
            String updateSql=ttlTime>0L?insertSql+" using TTL ?":insertSql;
            PreparedStatement insertstatement = targetSession.prepare(updateSql);
            BatchStatement batchstmt = new BatchStatement(BatchStatement.Type.LOGGED);
            final Session insertSession=targetSession;
            AbstractCassandraOperation operation = new AbstractCassandraOperation(){
                @Override
                protected void doInQuery(Row row) throws RuntimeException {
                    super.doInQuery(row);
                    if(ttlTime>0L){
                        map.put("ttl",ttlTime);
                    }
                    batchstmt.add(insertstatement.bind(map.values().toArray()));
                    rowPos++;
                    if (rowPos % batchSize == 0) {
                        if (!insertSession.execute(batchstmt).wasApplied()) {
                            throw new DAOException(" execute failed at " + rowPos);
                        }
                        batchstmt.clear();
                    }
                }

                @Override
                public void doAfter() {
                    if(!CollectionUtils.isEmpty(batchstmt.getStatements()) && !insertSession.execute(batchstmt).wasApplied()){
                        throw new RuntimeException("execute failed at last batch!");
                    }
                    batchstmt.clear();
                }
            };
            operation.setBatchSize(1000);
            operation.doOperationInQuery(sourceSession, selectSql, params);
            log.info("Batch processing row {}",operation.getRowPos());
        }catch (RuntimeException ex){
            ex.printStackTrace();
            log.error("{}",ex.getMessage());
        }finally {
            if(sourceSession!=null){
                sourceSession.close();
            }
            if(sourceCluster!=null){
                sourceCluster.close();
            }
            if(targetSession!=null){
                targetSession.close();
            }
            if(targetCluster!=null){
                targetCluster.close();
            }
        }
    }


}
