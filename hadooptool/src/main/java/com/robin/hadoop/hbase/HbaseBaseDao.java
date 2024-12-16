package com.robin.hadoop.hbase;

import com.robin.core.base.datameta.DataBaseColumnMeta;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@SuppressWarnings("unused")
public class HbaseBaseDao {
    private final Configuration config;
    private Configuration cfg;

    public static final int POOL_MAX_SIZE = 20;
    private static final String QUORUM="hbase.zookeeper.quorum";

    public HbaseBaseDao(String configfile) {
        config = new Configuration();
        if (configfile != null && !"".equals(configfile.trim())) {
            config.addResource(new Path(configfile));
        }
        cfg = HBaseConfiguration.create(config);
    }

    public HbaseBaseDao(Configuration config) {
        this.config = config;
        cfg = HBaseConfiguration.create(config);

    }

    public HbaseBaseDao() {
        config = new Configuration();
        cfg = HBaseConfiguration.create(config);
    }

    private Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(cfg);
    }

    public void createTable(HbaseTableParam tableParam) throws HbaseException {
        try (Admin admin = getConnection().getAdmin()) {
            if (admin.tableExists(TableName.valueOf(tableParam.getTableName()))) {
                throw new HbaseException("table exists");
            }
            List<HbaseParam> paramList = tableParam.getParamList();
            HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableParam.getTableName()));
            for (HbaseParam param : paramList) {
                HColumnDescriptor dc = new HColumnDescriptor(param.getFamily());
                if (param.isEnableBloom()) {
                    //enable bloom filter to hash row key
                    dc.setBloomFilterType(BloomType.ROW);
                }
                if (param.getCompressType() != null) {
                    dc.setCompressionType(param.getCompressType());
                }
                desc.addFamily(dc);
                if (param.getMaxversion() > 0) {
                    dc.setMaxVersions(param.getMaxversion());
                }
            }
            admin.createTable(desc);
        } catch (IOException e) {
            throw new HbaseException(e.getMessage());
        }
    }

    public byte[][] getHexSplits(String startKey, String endKey,
                                 int numRegions) {
        byte[][] splits = new byte[numRegions - 1][];
        BigInteger lowestKey = new BigInteger(startKey, 16);
        BigInteger highestKey = new BigInteger(endKey, 16);
        BigInteger range = highestKey.subtract(lowestKey);
        BigInteger regionIncrement = range.divide(BigInteger
                .valueOf(numRegions));
        lowestKey = lowestKey.add(regionIncrement);
        for (int i = 0; i < numRegions - 1; i++) {
            BigInteger key = lowestKey.add(regionIncrement.multiply(BigInteger
                    .valueOf(i)));
            byte[] b = String.format("%032x", key).toUpperCase().getBytes();
            splits[i] = b;
        }
        return splits;
    }

    public void addFamily(String tableName, String familyName) throws HbaseException {
        try (Admin admin = getConnection().getAdmin()) {
            if (!admin.tableExists(TableName.valueOf(tableName))) {
                throw new HbaseException("table not exists");
            }
            HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));
            HColumnDescriptor[] columndescArr = desc.getColumnFamilies();
            for (HColumnDescriptor columndesc : columndescArr) {
                String name = columndesc.getNameAsString();
                if (name.equalsIgnoreCase(familyName)) {
                    throw new HbaseException("familyName exists");
                }
            }
            admin.addColumn(TableName.valueOf(tableName), new HColumnDescriptor(familyName));
        } catch (IOException e) {
            throw new HbaseException(e.getMessage());
        }
    }

    public void putValue(String tableName, String familyName, String columnName, String key, String value) throws HbaseException {
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Put put = new Put(Bytes.toBytes(key));
            put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
            table.put(put);
        } catch (IOException e) {
            throw new HbaseException(e.getMessage());
        }
    }

    public void deleteColumn(String tableName, String familyName, String columnName) throws HbaseException {
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Delete delete = new Delete(Bytes.toBytes(tableName));
            delete.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
            table.delete(delete);
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    public boolean isTableExist(String tableName) throws HbaseException {
        boolean isexist = false;
        try (Admin admin = getConnection().getAdmin()) {
            if (admin.tableExists(TableName.valueOf(tableName))) {
                isexist = true;
            }
        } catch (Exception e) {
            throw new HbaseException(e);
        }
        return isexist;
    }

    public void putValue(HbaseTableParam param, Map<String, String> valueMap, String key) throws HbaseException {
        try {
            for (HbaseParam colparam : param.getParamList()) {
                String familyName = colparam.getFamily();
                for (String columnName : colparam.getColumnNameList()) {
                    if (valueMap.get(columnName) != null && !"".equals(valueMap.get(columnName).trim())) {
                        putValue(param.getTableName(), familyName, columnName, key, valueMap.get(columnName));
                    }
                }
            }

        } catch (Exception e) {
            throw new HbaseException(e);
        }

    }

    public boolean isKeyExists(String tableName, String key) throws HbaseException {
        boolean isexists = false;
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Scan scan = new Scan();
            Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(key.getBytes()));
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            Iterator<Result> iter = scanner.iterator();
            if (iter.hasNext()) {
                isexists = true;
            }
        } catch (IOException e) {
            throw new HbaseException(e);
        }
        return isexists;
    }

    public boolean isKeyInTable(String tableName, String keyval) throws HbaseException {
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(keyval));
            return table.exists(get);
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    public Result getResultByKey(String tableName, String keyval, String family) throws HbaseException {
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(keyval));
            get.addFamily(Bytes.toBytes(family));
            return table.get(get);
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    public Result getResultByKey(Table table, String keyval) throws HbaseException {
        try {
            Get get = new Get(Bytes.toBytes(keyval));
            return table.get(get);
        } catch (IOException e) {
            throw new HbaseException(e);
        }
    }

    public Map<String, String> getResultValueByKey(String tableName, String familyName, String keyval) throws HbaseException {
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(keyval));
            Result result = table.get(get);
            NavigableMap<byte[], byte[]> map = result.getFamilyMap(Bytes.toBytes(familyName));
            Iterator<byte[]> keyiter = map.keySet().iterator();
            Map<String, String> tmpMap = new HashMap<>();
            while (keyiter.hasNext()) {
                String key = new String(keyiter.next());
                String val = new String(map.get(key.getBytes()));
                tmpMap.put(key, val);
            }
            return tmpMap;
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    public List<Map<String, String>> getArrayFromFamily(String tableName, String familyName, String keyvalue) throws HbaseException {
        List<Map<String, String>> retList = new ArrayList<>();
        Scan scan = new Scan();
        Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(keyvalue.getBytes()));
        scan.setFilter(filter);
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result rs : scanner) {
                NavigableMap<byte[], byte[]> map = rs.getFamilyMap(Bytes.toBytes(familyName));
                Iterator<byte[]> keyiter = map.keySet().iterator();
                Map<String, String> tmpMap = new HashMap<>();
                while (keyiter.hasNext()) {
                    String key = new String(keyiter.next());
                    String val = new String(map.get(key.getBytes()));
                    tmpMap.put(key, val);
                }
                retList.add(tmpMap);
            }
            return retList;
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    /**
     * GetAll record by family key
     *
     * @param tableName
     * @param familyName
     * @param keyName    key field in map
     * @param keyList    query field List
     * @return
     */
    public List<Map<String, String>> getAllRecord(String tableName, String familyName, String keyName, List<String> keyList) throws HbaseException {
        List<Map<String, String>> retMapList = new ArrayList<>();
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes(familyName));
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
             ResultScanner scanner = table.getScanner(scan)) {
            Iterator<Result> iter = scanner.iterator();
            List<byte[]> keybyteList = new ArrayList<>();
            int pos = 1;
            keyList.add(keyName);
            keybyteList.add(keyName.getBytes());
            while (iter.hasNext()) {
                Map<String, String> retmap = new HashMap<>();
                Result rs = iter.next();
                String keyValue = new String(rs.getRow());
                retmap.put(keyName, keyValue);
                NavigableMap<byte[], byte[]> map = rs.getFamilyMap(Bytes.toBytes(familyName));
                if (pos == 1) {
                    for (NavigableMap.Entry<byte[], byte[]> entry : map.entrySet()) {
                        keybyteList.add(entry.getValue());
                        String key = new String(entry.getKey());
                        keyList.add(key);
                        retmap.put(key, new String(entry.getValue()));
                    }
                } else {
                    for (int i = 1; i < keyList.size(); i++) {
                        String value = map.get(keybyteList.get(i)) == null ? "" : new String(map.get(keybyteList.get(i)));
                        retmap.put(keyList.get(i), value);
                    }
                }
                pos++;
                retMapList.add(retmap);
            }
            return retMapList;
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    public List<Map<String, String>> getRecordByKey(String tableName, String keyValue, Map<String, String> valueFilterMap) throws HbaseException {
        List<Map<String, String>> retMapList = new ArrayList<>();
        Scan scan = new Scan();
        scan.setMaxVersions(10000);
        Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(keyValue)));
        FilterList filterlist = generateFamilyFilter(valueFilterMap);
        filterlist.addFilter(filter);
        scan.setFilter(filterlist);
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
             ResultScanner scanner = table.getScanner(scan)) {
            Map<String, Map<String, List<String>>> tmpmap1 = new HashMap<>();
            List<String> keyList = new ArrayList<>();
            List<String> fnameList = new ArrayList<>();
            for (Result rs : scanner) {
                extract(tmpmap1, keyList, fnameList, rs);
                for (String key : keyList) {
                    String firstName = fnameList.get(0);

                    List<String> valueList = tmpmap1.get(firstName).get(key);

                    for (int j = 0; j < valueList.size(); j++) {

                        Map<String, String> tmpmap = new HashMap<>();
                        tmpmap.put(firstName, valueList.get(j));
                        for (int k = 1; k < fnameList.size(); k++) {
                            List<String> list2 = tmpmap1.get(fnameList.get(k)).get(key);
                            if (!list2.isEmpty() && list2.size() > j) {
                                tmpmap.put(fnameList.get(k), list2.get(j));
                            }
                        }
                        retMapList.add(tmpmap);
                    }
                }
            }
            return retMapList;
        } catch (Exception e) {
            throw new HbaseException(e);
        }

    }

    private void extract(Map<String, Map<String, List<String>>> tmpmap1, List<String> keyList, List<String> fnameList, Result rs) {
        Cell[] kv1 = rs.rawCells();
        for (Cell cell : kv1) {
            String key = new String(cell.getQualifierArray());
            String value = new String(cell.getValueArray());
            String fname = new String(cell.getFamilyArray());
            if (!tmpmap1.containsKey(fname)) {
                Map<String, List<String>> map1 = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(value);
                map1.put(key, list);
                tmpmap1.put(fname, map1);
                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
                if (!fnameList.contains(fname)) {
                    fnameList.add(fname);
                }
            } else {
                if (!tmpmap1.get(fname).containsKey(key)) {
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    tmpmap1.get(fname).put(key, list);
                } else {
                    tmpmap1.get(fname).get(key).add(value);
                }
                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            }
        }
    }

    public List<Map<String, String>> getRecordByColumn(String tableName, Map<String, String> valueFilterMap) throws HbaseException {
        List<Map<String, String>> retMapList = new ArrayList<>();
        Scan scan = new Scan();
        scan.setMaxVersions(1);
        FilterList filterlist = generateFamilyFilter(valueFilterMap);
        scan.setFilter(filterlist);
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
             ResultScanner scanner = table.getScanner(scan)) {
            Map<String, Map<String, List<String>>> tmpmap1 = new HashMap<>();
            List<String> keyList = new ArrayList<>();
            List<String> fnameList = new ArrayList<>();
            for (Result rs : scanner) {
                tmpmap1.clear();
                extract(tmpmap1, keyList, fnameList, rs);
                for (int i = 0; i < keyList.size(); i++) {
                    String key = keyList.get(i);
                    String fristName = fnameList.get(i);

                    List<String> valueList = tmpmap1.get(fristName).get(key);

                    for (int j = 0; j < valueList.size(); j++) {
                        if (i == 0) {
                            Map<String, String> tmpmap = new HashMap<>();
                            tmpmap.put(fristName, valueList.get(j));
                            retMapList.add(tmpmap);
                        } else {
                            retMapList.get(j).put(fristName, valueList.get(j));
                        }
                    }
                }
            }
            return retMapList;
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    @SuppressWarnings("unused")
    public int getRecordCountByColumn(String tableName, Map<String, String> valueFilterMap) throws HbaseException {
        Scan scan = new Scan();
        scan.setMaxVersions(1);
        FilterList filterlist = generateFamilyFilter(valueFilterMap);
        scan.setFilter(filterlist);
        try (Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
             ResultScanner scanner = table.getScanner(scan)){
            int count = 0;
            for (Result rs : scanner) {
                count++;
            }
            return count;
        } catch (Exception e) {
            throw new HbaseException(e);
        }
    }

    private FilterList generateFamilyFilter(Map<String, String> valueMap) {
        FilterList filterlist = new FilterList();
        if (ObjectUtils.isEmpty(valueMap)) {
            return filterlist;
        }
        for (Map.Entry<String, String> stringStringEntry : valueMap.entrySet()) {
            Filter filter;
            String oper = stringStringEntry.getValue();
            String[] arr = oper.split(",");
            filter = new SingleColumnValueFilter(Bytes.toBytes(stringStringEntry.getKey()), Bytes.toBytes(arr[0]), CompareOp.EQUAL, Bytes.toBytes(arr[1]));
            filterlist.addFilter(filter);
        }
        return filterlist;
    }




    public Map<String, Map<String, String>> getRegionResult(String tableName, byte[] startKey, byte[] endKey, String fieldArr, List<String> keyList) throws HbaseException {
        Map<String, Map<String, String>> retMap = new HashMap<>();
        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        String[] fields = fieldArr.split(",");
        for (String field : fields) {
            String[] arr = field.split(":");
            scan.addColumn(arr[0].getBytes(), arr[1].getBytes());
        }
        try(Connection conn = getConnection(); Table table = conn.getTable(TableName.valueOf(tableName));
            ResultScanner scanner = table.getScanner(scan)) {
            for (Result rs : scanner) {
                String key = new String(rs.getRow());
                Map<String, String> valueMap = new HashMap<>();
                for (Cell val : rs.listCells()) {
                    valueMap.put(new String(val.getQualifierArray()), new String(val.getValueArray()));
                }
                if (keyList != null) {
                    keyList.add(key);
                }
                retMap.put(key, valueMap);
            }
            return retMap;
        } catch (Exception ex) {
            throw new HbaseException(ex);
        }
    }

    public void truncate(String tableName) throws HbaseException {
        try(Admin admin=getConnection().getAdmin()) {
            TableName tabName=TableName.valueOf(tableName);
            List<HRegionInfo> regions=admin.getTableRegions(tabName);
            byte[][] regionKeys = new byte[regions.size()][];
            int i = 0;
            while (i<regions.size()) {
                regionKeys[i]=regions.get(i).getStartKey();
                i++;
            }
            HTableDescriptor ds= admin.getTableDescriptor(TableName.valueOf(tableName));
            admin.disableTable(tabName);
            admin.deleteTable(tabName);
            admin.createTable(ds, regionKeys);
        } catch (IOException ex) {
            throw new HbaseException(ex);
        }
    }
    private Filter getFilterByMeta(String columnName,String cmpColumn,String value,DataBaseColumnMeta meta){
        return null;
    }

    public static KeyValue createKeyValue(String family, String key, String colName, String value) {
        return new KeyValue(Bytes.toBytes(key), Bytes.toBytes(family), Bytes.toBytes(colName),
                System.currentTimeMillis(), Bytes.toBytes(value));
    }

}
