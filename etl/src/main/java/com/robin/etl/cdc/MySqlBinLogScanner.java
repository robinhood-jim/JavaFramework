package com.robin.etl.cdc;

import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.*;
import com.google.gson.Gson;
import com.robin.comm.resaccess.writer.QueueWriterFactory;
import com.robin.comm.util.config.YamlUtils;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractQueueWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.util.ObjectUtils;

@Slf4j
public class MySqlBinLogScanner implements Callable<Integer> {
    private Map<String,Object> configMap;
    private String dataSourceName;
    private BaseDataBaseMeta meta;

    private BinaryLogClient client;
    private Map<Long, String> tableeventMap = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
    private AbstractQueueWriter writer;
    private Map<String, List<Integer>> syncMap = new HashMap<>();
    private boolean stopTag=false;
    private Map<String, Map<Integer, DataBaseColumnMeta>> tableCfgMap = new HashMap<>();
    private Map<String, List<DataBaseColumnMeta>> tableColumnMap = new HashMap<>();
    private Gson gson= GsonUtil.getGson();
    private BinLogInfo info;
    private DataCollectionMeta collectionMeta;

    public MySqlBinLogScanner(InputStream configStream){
        collectionMeta=YamlUtils.loadFromStream(configStream,DataCollectionMeta.class);
        configMap=collectionMeta.getResourceCfgMap();
        initParam();
    }
    private void initParam(){
        if (configMap.containsKey("binlog")) {
            Map<String, Object> tmap = (Map<String, Object>) configMap.get("binlog");
            if (tmap.containsKey("dbconfig")) {
                Map<String,Object> dbConfigMap = (Map<String, Object>) tmap.get("dbconfig");
                int port = 0;
                if(dbConfigMap.containsKey("sourceName")){
                    dataSourceName=dbConfigMap.get("sourceName").toString();
                }
                if (dbConfigMap.containsKey("port")) {
                    port = Integer.valueOf(dbConfigMap.get("port").toString());
                }

                DataBaseParam param = new DataBaseParam(dbConfigMap.get("hostName").toString(), port, dbConfigMap.get("dbName").toString(), dbConfigMap.get("username").toString(), dbConfigMap.get("password").toString());
                meta = DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
            }
        }

        try {
            writer = QueueWriterFactory.getQueueWrite(collectionMeta.getFsType(), collectionMeta);
            writer.initalize();
        }catch (IOException ex){
            throw new OperationNotSupportException(ex);
        }
    }

    @Override
    public Integer call() throws Exception {
        try {
            client = new BinaryLogClient(meta.getParam().getHostName(), meta.getParam().getPort(), meta.getParam().getUserName(), meta.getParam().getPasswd());
            log.info (" after init client ");
            info=getBinLogInfo();
            log.info("load previous config ",info);
            if(info!=null){
                client.setBinlogFilename(info.getBinLogName());
                client.setServerId(info.getServerId());
                client.setBinlogPosition(info.getPosition());
            }

            EventDeserializer eventDeserializer = new EventDeserializer();
            eventDeserializer.setCompatibilityMode(EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY,
                    EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG);
            client.registerEventListener(event -> {
                EventType type = event.getHeader().getEventType();
                log.info("incoming event {}", event);
                Serializable[] objs = null;
                try {
                    switch (type) {
                        case TABLE_MAP:
                            TableMapEventData data = event.getData();
                            if (canTableBeScanned(data.getTable())) {
                                tableeventMap.put(data.getTableId(), data.getTable());
                            }
                            break;
                        case WRITE_ROWS:
                        case EXT_WRITE_ROWS:
                            WriteRowsEventData writeEventData = event.getData();
                            long tableId = writeEventData.getTableId();
                            if (canTableBeScannedWithType(tableeventMap.get(tableId), 1)) {
                                Map<String, Object> recordMap = parseRecord(tableId, writeEventData.getIncludedColumns(), writeEventData.getRows().get(0));
                                writer.writeMessage(null, recordMap);
                            }
                            break;
                        case UPDATE_ROWS:
                        case EXT_UPDATE_ROWS:
                            UpdateRowsEventData updateData = event.getData();
                            Long updateTabId = updateData.getTableId();
                            if (canTableBeScannedWithType(tableeventMap.get(updateTabId), 2)) {
                                List<Map.Entry<Serializable[], Serializable[]>> list = updateData.getRows();
                                Map<String, Object> recordMap = parseRecord(updateTabId, updateData.getIncludedColumns(), list.get(0).getValue());
                                recordMap.put("_UPDATE", true);
                                writer.writeMessage(null, recordMap);
                            }
                            break;
                        case DELETE_ROWS:
                        case EXT_DELETE_ROWS:
                            DeleteRowsEventData deleteData = event.getData();
                            Long deleteTabId = deleteData.getTableId();
                            if (canTableBeScannedWithType(tableeventMap.get(deleteTabId), 3)) {
                                Map<String, Object> recordMap = parseRecord(deleteTabId, deleteData.getIncludedColumns(), deleteData.getRows().get(0));
                                recordMap.put("_DELETE", true);
                                writer.writeMessage(null, recordMap);
                            }
                            break;
                        case QUERY:
                            QueryEventData queryEventData = event.getData();
                            String database = queryEventData.getDatabase();
                            EventHeaderV4 headerV4=event.getHeader();
                            int errCode = queryEventData.getErrorCode();
                            String sql = queryEventData.getSql();
                            log.info("get Query Sql {}",sql);
                            if (errCode == 0) {
                                if (!sql.equalsIgnoreCase("BEGIN")) {
                                    if(sql.toLowerCase().startsWith("insert") || sql.toLowerCase().startsWith("update") ||sql.toLowerCase().startsWith("delete")){
                                        Map<String, Object> recordMap=new HashMap<>();
                                        recordMap.put("sql",sql);
                                        recordMap.put("ts",headerV4.getTimestamp());
                                        recordMap.put("database",database);
                                        recordMap.put("pos",headerV4.getPosition());
                                        recordMap.put("serverId",headerV4.getServerId());
                                        writer.writeMessage(null, recordMap);
                                    }
                                }
                            }
                            break;
                        default:
                            if(type!=EventType.FORMAT_DESCRIPTION && type!=EventType.HEARTBEAT) {
                                if(type==EventType.ROTATE) {
                                    EventData eventData = event.getData();
                                    RotateEventData rotateEventData;
                                    if (eventData instanceof EventDeserializer.EventDataWrapper) {
                                        rotateEventData = (RotateEventData) ((EventDeserializer.EventDataWrapper) eventData).getInternal();
                                    } else {
                                        rotateEventData = (RotateEventData) eventData;
                                    }
                                    long logPos = rotateEventData.getBinlogPosition();
                                    String binlogFileName = rotateEventData.getBinlogFilename();
                                    logBinlog(binlogFileName,logPos,info.getServerId());
                                    log.info("binlog rotate to " + rotateEventData.getBinlogFilename() + " and pos " + rotateEventData.getBinlogPosition());
                                }else{
                                    EventHeaderV4 header = event.getHeader();
                                    long position = header.getPosition();
                                    long serverId = header.getServerId();
                                    logBinlog(info.getBinLogName(),position,serverId);
                                }
                            }
                    }
                }catch (Exception ex){

                }
            });
            client.setEventDeserializer(eventDeserializer);
            client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
                @Override
                public void onConnect(BinaryLogClient binaryLogClient) {
                    info=getBinLogInfo();
                    log.info("load config ",info);
                }

                @Override
                public void onCommunicationFailure(BinaryLogClient binaryLogClient, Exception e) {
                    log.error("bin log client Communicate failed!");
                    logBinlog(client.getBinlogFilename(),client.getBinlogPosition(),client.getServerId());
                }

                @Override
                public void onEventDeserializationFailure(BinaryLogClient binaryLogClient, Exception e) {
                    logBinlog(client.getBinlogFilename(),client.getBinlogPosition(),client.getServerId());
                }

                @Override
                public void onDisconnect(BinaryLogClient binaryLogClient) {
                    log.error("bin log client disconnected!");
                    logBinlog(client.getBinlogFilename(),client.getBinlogPosition(),client.getServerId());
                }
            });
            client.setKeepAlive(true);
            client.connect(DEFAULT_TIMEOUT);
            while (!stopTag){
                TimeUnit.SECONDS.sleep(1);
            }
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
            ex.printStackTrace();
        }finally {
            if(client!=null){
                client.disconnect();
            }
        }
        return 0;
    }
    private boolean canTableBeScanned(String tableName) {
        if (!syncMap.isEmpty()) {
            if ((syncMap.containsKey(tableName))) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    private Map<String, Object> parseRecord(long tableId, BitSet includeColumns, Serializable[] rows) {
        List<Integer> columnposList = getAvailableColumn(includeColumns);
        Map<String, Object> map = new HashMap<String, Object>();
        String tableName = tableeventMap.get(tableId);
        return retrieveRecord(tableName, rows, columnposList);
    }
    public List<Integer> getAvailableColumn(BitSet columnbit) {
        List<Integer> posList = new ArrayList<>();

        for (int i = 0; i < columnbit.length(); i++) {
            if (columnbit.get(i)) {
                posList.add(i);
            }
        }
        return posList;
    }
    private Map<String, Object> retrieveRecord(String tableName, Serializable[] records, List<Integer> columnposList) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {

            for (int i = 0; i < records.length; i++) {
                int pos = columnposList.get(i);
                DataBaseColumnMeta meta = tableColumnMap.get(tableName).get(pos);
                if(records[i] instanceof byte[]){
                    map.put(meta.getColumnName(),new String((byte[])records[i]));
                }else
                    map.put(meta.getColumnName(), records[i]);
            }
            map.put("dataSourceName",dataSourceName);
            map.put("tableName",tableName);
        } catch (Exception ex) {
            log.error("{}",ex.getMessage());
        }
        return map;
    }



    private boolean canTableBeScannedWithType(String tableName, Integer dataType) {
        if (!syncMap.isEmpty()) {
            if ((syncMap.containsKey(tableName))) {
                if (syncMap.get(tableName).contains(dataType)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    private void logBinlog(String binlogFileName,Long position,Long serverId) {
        String tmpPath=System.getProperty("user.dir")+ File.separator+"binlog.json";
        Map<String,Object> map=new HashMap<>();
        if(info!=null){
            info.setBinLogName(binlogFileName);
            info.setPosition(position);
            info.setServerId(serverId);
        }
        map.put("binLogName",binlogFileName);
        map.put("position",position);
        map.put("serverId",serverId);
        try{
            Files.writeString(Path.of(tmpPath),gson.toJson(map), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }
    }
    private BinLogInfo getBinLogInfo(){
        String tmpPath=System.getProperty("user.dir")+ File.separator+"binlog.json";
        log.info("get binlog config from file {}",tmpPath);
        try {
            if (Files.exists(Path.of(tmpPath))) {
                String json = Files.readString(Path.of(tmpPath));
                Map<String,Object> map=gson.fromJson(json,new TypeToken<Map<String,Object>>(){}.getType());
                return new BinLogInfo(map);
            }else{
                log.info("file does not exists!");
            }
        }catch (IOException ex){

        }
        return null;
    }
    private BinLogInfo readDefaultBinLog(BinaryLogClient client){
        return new BinLogInfo(client.getBinlogFilename(),client.getBinlogPosition(),client.getServerId());
    }
    public void stop(){
        stopTag=true;
    }
    @Getter
    @Setter
    private class BinLogInfo{
        private long serverId;
        private long position;
        private String binLogName;
        public BinLogInfo(String binLogName,long position,long serverId){
            this.binLogName=binLogName;
            this.position=position;
            this.serverId=serverId;
        }
        public BinLogInfo(Map<String,Object> map){
            if(!ObjectUtils.isEmpty(map.get("binLogName"))){
                binLogName=map.get("binLogName").toString();
            }
            if(!ObjectUtils.isEmpty(map.get("position"))){
                position=Long.parseLong(map.get("position").toString());
            }
            if(!ObjectUtils.isEmpty(map.get("serverId"))){
                serverId=Long.parseLong(map.get("serverId").toString());
            }
        }

    }
    public static void main(String[] args){
        ListeningExecutorService pool= MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
        ListenableFuture<Integer> future=pool.submit(new MySqlBinLogScanner(MySqlBinLogScanner.class.getClassLoader().getResourceAsStream("metadata.yml")));
        Futures.addCallback(future, new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable Integer integer) {

            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("{}",throwable.getMessage());
            }
        },pool);
        try{
            Integer ret=future.get();
            System.out.println(ret);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
