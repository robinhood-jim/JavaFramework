package com.robin.hadoop.hbase;

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

import java.math.BigInteger;
import java.util.*;

public class HbaseBaseDao {
	private Configuration config;
	private final static Logger LOGGER=LoggerFactory.getLogger(HbaseBaseDao.class);
	private Configuration cfg=null;

	public static final int POOL_MAX_SIZE=20; 
	public HbaseBaseDao(String configfile){
		config=new Configuration();
		if(configfile!=null &&!"".equals(configfile.trim())){
			config.addResource(new Path(configfile));
		}
		cfg=HBaseConfiguration.create(config);
	}
	public HbaseBaseDao(Configuration config){
		this.config=config;
		cfg=HBaseConfiguration.create(config);
		if(config.get("hbase.zookeeper.quorum")!=null){
			LOGGER.info("get quorum from config");
			cfg.set("hbase.zookeeper.quorum", config.get("hbase.zookeeper.quorum"));
		}
	}
	public HbaseBaseDao(){
		config=new Configuration();
		cfg=HBaseConfiguration.create(config);
	}
	public void createTable(HbaseTableParam tableParam) throws HbaseException{
		try{
			HBaseAdmin admin=new HBaseAdmin(cfg);
			if(admin.tableExists(tableParam.getTableName())){
				admin.close();
				throw new HbaseException("table exists");
			}
			List<HbaseParam> paramList=tableParam.getParamList();
			HTableDescriptor desc=new HTableDescriptor(TableName.valueOf(tableParam.getTableName()));
			for (HbaseParam param:paramList) {
				HColumnDescriptor dc=new HColumnDescriptor(param.getFamily());
				if(param.isEnableBloom()){
					//enable bloom filter to hash row key
					dc.setBloomFilterType(BloomType.ROW);
				}
				if(param.getCompressType()!=null) {
                    dc.setCompressionType(param.getCompressType());
                }
				desc.addFamily(dc);
				if(param.getMaxversion()>0){
					dc.setMaxVersions(param.getMaxversion());
				}
			}
			admin.createTable(desc);
			admin.close();
		}catch (Exception e) {
			LOGGER.error("",e);
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
	public void addFamily(String tableName,String familyName) throws HbaseException{
		try{
			HBaseAdmin admin=new HBaseAdmin(cfg);
			if(!admin.tableExists(tableName)){
				admin.close();
				throw new HbaseException("table not exists");
			}
			HTableDescriptor desc=new HTableDescriptor(TableName.valueOf(tableName));
			HColumnDescriptor[] columndescArr= desc.getColumnFamilies();
			for (int i = 0; i < columndescArr.length; i++) {
				HColumnDescriptor columndesc=columndescArr[i];
				String name=columndesc.getNameAsString();
				if(name.equalsIgnoreCase(familyName)){
					admin.close();
					throw new HbaseException("familyName exists");
				}
			}
			admin.addColumn(tableName, new HColumnDescriptor(familyName));
			admin.close();
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e.getMessage());
		}
	}
	public void putValue(String tableName,String familyName,String columnName,String key,String value) throws HbaseException{
		Connection conn=null;
		Table table=null;
		try{
			conn=ConnectionFactory.createConnection(cfg);
			table=conn.getTable(TableName.valueOf(tableName));
			Put put=new Put(Bytes.toBytes(key));
			put.add(Bytes.toBytes(familyName), Bytes.toBytes(columnName), Bytes.toBytes(value));
			table.put(put);
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e.getMessage());
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex){
				
			}
		}
	}
	public void deleteColumn(String tableName,String familyName,String columnName) throws HbaseException{
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Delete delete=new Delete(Bytes.toBytes(tableName));
			delete.deleteColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
			table.delete(delete);
			table.close();
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e);
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex){
				
			}
		}
	}
	public boolean isTableExist(String tableName) throws HbaseException{
		boolean isexist=false;
		try{
			HBaseAdmin admin=new HBaseAdmin(cfg);
			if(admin.tableExists(tableName)){
				isexist=true;
			}
			admin.close();
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e);
		}
		return isexist;
	}
	public void putValue(HbaseTableParam param,Map<String,String> valueMap,String key) throws HbaseException{
		try{
			for(HbaseParam colparam:param.getParamList()){
				String familyName=colparam.getFamily();
				for(String columnName:colparam.getColumnNameList()){
					if(valueMap.get(columnName)!=null && !"".equals(valueMap.get(columnName).trim())){
						putValue(param.getTableName(),familyName,columnName,key,valueMap.get(columnName));
					}
				}
			}
		
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e);
		}
		
	}
	public boolean isKeyexistes(String tableName,String key) throws HbaseException{
		boolean isexists=false;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			Filter filter=new RowFilter(CompareOp.EQUAL,new BinaryComparator(key.getBytes()));
			scan.setFilter(filter);
			ResultScanner scanner=table.getScanner(scan);
			Iterator<Result> iter=scanner.iterator();
			if(iter.hasNext()) {
                isexists=true;
            }
		}catch (Exception e) {
			throw new HbaseException(e);
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex){
				
			}
		}
		return isexists;
	}
	public boolean isKeyInTable(String tableName,String keyval) throws HbaseException{
		boolean isok=true;
		Exception ex;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Get get=new Get(Bytes.toBytes(keyval));
			return table.exists(get);
		}catch (Exception e) {
			isok=false;
			ex=e;
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		
		if(!isok) {
            throw new HbaseException(ex);
        } else {
            return false;
        }
	}
	public Result getResultByKey(String tableName,String keyval,String family) throws HbaseException{
		boolean isok=true;
		Exception ex=null;
		Result result=null;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Get get=new Get(Bytes.toBytes(keyval));
			get.addFamily(Bytes.toBytes(family));
			result=table.get(get);
		}catch (Exception e) {
			isok=false;
			ex=e;
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		if(!isok) {
            throw new HbaseException(ex);
        } else {
            return result;
        }
	}
	public Result getResultByKey(HTableInterface table,String keyval) throws HbaseException{
		boolean isok=true;
		Exception ex=null;
		Result result=null;
		try{
			Get get=new Get(Bytes.toBytes(keyval));
			result=table.get(get);
		}catch (Exception e) {
			isok=false;
			ex=e;
		}
		if(!isok) {
            throw new HbaseException(ex);
        } else {
            return result;
        }
	}
	
	public Map<String, String> getResultValueByKey(String tableName,String familyName,String keyval) throws HbaseException{
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Get get=new Get(Bytes.toBytes(keyval));
			Result result=table.get(get);
			NavigableMap<byte[], byte[]> map=result.getFamilyMap(Bytes.toBytes(familyName));
			Iterator<byte[]> keyiter=map.keySet().iterator();
			Map<String, String> tmpMap=new HashMap<String, String>();
			while(keyiter.hasNext()){
				String key=new String(keyiter.next());
				String val=new String(map.get(key.getBytes()));
				tmpMap.put(key, val);
			}	
			return tmpMap;
		}catch (Exception e) {
			throw new HbaseException(e);
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
	}
	public List<Map<String, String>> getArrayFromFamily(String tableName,String familyName,String keyvalue) throws HbaseException{
		List<Map<String,String>> retList=new ArrayList<Map<String,String>>();
		ResultScanner scanner=null;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			Filter filter=new RowFilter(CompareOp.EQUAL,new BinaryComparator(keyvalue.getBytes()));
			scan.setFilter(filter);
			scanner=table.getScanner(scan);
			Iterator<Result> iter=scanner.iterator();
			while(iter.hasNext()){
				Result rs=iter.next();
				NavigableMap<byte[], byte[]> map=rs.getFamilyMap(Bytes.toBytes(familyName));
				Iterator<byte[]> keyiter=map.keySet().iterator();
				Map<String, String> tmpMap=new HashMap<String, String>();
				while(keyiter.hasNext()){
					String key=new String(keyiter.next());
					String val=new String(map.get(key.getBytes()));
					tmpMap.put(key, val);
				}	
				retList.add(tmpMap);
			}
		}catch (Exception e) {
			LOGGER.error("",e);
			throw new HbaseException(e);
		}finally{
			if(scanner!=null){
				scanner.close();
			}
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return retList;
	}
	/**
	 * GetAll record by family key
	 * @param tableName   
	 * @param familyName  
	 * @param keyName     key field in map
	 * @param keyList     query field List
	 * @return
	 */
	public List<Map<String, String>> getAllRecord(String tableName,String familyName,String keyName,List<String> keyList){
		List<Map<String, String>> retMapList=new ArrayList<Map<String,String>>();
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			scan.addFamily(Bytes.toBytes(familyName));
			ResultScanner scanner=table.getScanner(scan);
			Iterator<Result> iter=scanner.iterator();
			List<byte[]> keybyteList=new ArrayList<byte[]>();
			int pos=1;
			keyList.add(keyName);
			keybyteList.add(keyName.getBytes());
			while(iter.hasNext()){
				Map<String, String> retmap=new HashMap<String, String>();
				Result rs=iter.next();
				String keyValue=new String(rs.getRow());
				retmap.put(keyName, keyValue);
				NavigableMap<byte[], byte[]> map=rs.getFamilyMap(Bytes.toBytes(familyName));
				if(pos==1){
					Iterator<byte[]> keyiter=map.keySet().iterator();
					while(keyiter.hasNext()){
						byte[] keybyte=keyiter.next();
						keybyteList.add(keybyte);
						String key=new String(keybyte);
						keyList.add(key);
						retmap.put(key,new String(map.get(keybyte)));
					}	
				}else
				{
					for(int i=1;i<keyList.size();i++){
						String value=map.get(keybyteList.get(i))==null?"":new String(map.get(keybyteList.get(i)));
						retmap.put(keyList.get(i),value);
					}
				}
				pos++;
				retMapList.add(retmap);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return retMapList;
	}
	public List<Map<String, String>> getRecordByKey(String tableName,String keyValue,Map<String, String> valueFilterMap){
		List<Map<String, String>> retMapList=new ArrayList<Map<String,String>>();
		ResultScanner scanner=null;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			scan.setMaxVersions(10000);
			Filter filter=new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(keyValue)));
			FilterList filterlist=generateFamilyFilter(valueFilterMap);
			filterlist.addFilter(filter);
			scan.setFilter(filterlist);
			scanner=table.getScanner(scan);
			//Map<String, List<String>> valueMap=new HashMap<String, List<String>>();
			Map<String, Map<String, List<String>>> tmpmap1=new HashMap<String, Map<String,List<String>>>();
			List<String> keyList=new ArrayList<String>();
			List<String> fnameList=new ArrayList<String>();
			for (Result rs:scanner) {
				   Cell[]  kv1=rs.rawCells();
				   for (int i = 0; i < kv1.length; i++) {
					 String key=new String(kv1[i].getQualifierArray());
					 String value=new String(kv1[i].getValueArray());
					 String fname=new String(kv1[i].getFamilyArray());
					 if(!tmpmap1.containsKey(fname)){
						 	Map<String, List<String>> map1=new HashMap<String, List<String>>();
							List<String> list=new ArrayList<String>();
							list.add(value);
							map1.put(key, list);
							tmpmap1.put(fname, map1);
							if(!keyList.contains(key)) {
                                keyList.add(key);
                            }
							if(!fnameList.contains(fname)){
								fnameList.add(fname);
							}
						}else{
							if(!tmpmap1.get(fname).containsKey(key)){
								List<String> list=new ArrayList<String>();
								list.add(value);
								tmpmap1.get(fname).put(key, list);
							}else{
								tmpmap1.get(fname).get(key).add(value);
							}
							if(!keyList.contains(key)) {
                                keyList.add(key);
                            }
						}
				   }
				for (int i = 0; i < keyList.size(); i++) {
					String key=keyList.get(i);
					String fristName=fnameList.get(0);
					
					List<String> valueList=tmpmap1.get(fristName).get(key);
					
					for (int j = 0; j < valueList.size(); j++) {
						
						Map<String, String> tmpmap=new HashMap<String, String>();
						tmpmap.put(fristName, valueList.get(j));
						for (int k = 1; k < fnameList.size(); k++) {
							List<String> list2=tmpmap1.get(fnameList.get(k)).get(key);
							if(!list2.isEmpty() && list2.size()>j){
								tmpmap.put(fnameList.get(k), list2.get(j));
							}
						};
						retMapList.add(tmpmap);
				}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(scanner!=null){
				scanner.close();
			}
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return retMapList;
	}
	public List<Map<String, String>> getRecordByColumn(String tableName,Map<String, String> valueFilterMap){
		List<Map<String, String>> retMapList=new ArrayList<Map<String,String>>();
		ResultScanner scanner=null;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			scan.setMaxVersions(1);
			
			FilterList filterlist=generateFamilyFilter(valueFilterMap);
			scan.setFilter(filterlist);
			scanner=table.getScanner(scan);
			Map<String, Map<String, List<String>>> tmpmap1=new HashMap<String, Map<String,List<String>>>();
			List<String> keyList=new ArrayList<String>();
			List<String> fnameList=new ArrayList<String>();
			for (Result rs:scanner) {
				tmpmap1.clear();
				   Cell[]  kv1=rs.rawCells();
				   for (int i = 0; i < kv1.length; i++) {
					 String key=new String(kv1[i].getQualifierArray());
					 String value=new String(kv1[i].getValueArray());
					 String fname=new String(kv1[i].getFamilyArray());
					 if(!tmpmap1.containsKey(fname)){
						 	Map<String, List<String>> map1=new HashMap<String, List<String>>();
							List<String> list=new ArrayList<String>();
							list.add(value);
							map1.put(key, list);
							tmpmap1.put(fname, map1);
							if(!keyList.contains(key)) {
                                keyList.add(key);
                            }
							if(!fnameList.contains(fname)){
								fnameList.add(fname);
							}
						}else{
							if(!tmpmap1.get(fname).containsKey(key)){
								List<String> list=new ArrayList<String>();
								list.add(value);
								tmpmap1.get(fname).put(key, list);
							}else{
								tmpmap1.get(fname).get(key).add(value);
							}
							if(!keyList.contains(key)) {
                                keyList.add(key);
                            }
						}
				   }
				
				
				for (int i = 0; i < keyList.size(); i++) {
					String key=keyList.get(i);
					String fristName=fnameList.get(i);
					
					List<String> valueList=tmpmap1.get(fristName).get(key);
					
					for (int j = 0; j < valueList.size(); j++) {
						if(i==0){
						Map<String, String> tmpmap=new HashMap<String, String>();
						tmpmap.put(fristName, valueList.get(j));
						retMapList.add(tmpmap);
						}else{
							retMapList.get(j).put(fristName, valueList.get(j));
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(scanner!=null){
				scanner.close();
			}
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return retMapList;
	}
	@SuppressWarnings("unused")
	public int  getRecordCountByColumn(String tableName,Map<String, String> valueFilterMap){
		ResultScanner scanner=null;
		int count=0;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			scan.setMaxVersions(1);
			FilterList filterlist=generateFamilyFilter(valueFilterMap);
			scan.setFilter(filterlist);
			scanner=table.getScanner(scan);
			for (Result rs:scanner) {
				count++;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(scanner!=null){
				scanner.close();
			}
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return count;
	}
	private FilterList generateFamilyFilter(Map<String, String> valueMap){
		FilterList filterlist=new FilterList();
		if(valueMap==null || valueMap.isEmpty()) {
            return filterlist;
        }
		Iterator<String> iter=valueMap.keySet().iterator();
		while(iter.hasNext()){
			Filter filter=null;
			String familyName=iter.next();
			String oper=valueMap.get(familyName);
			String[] arr=oper.split(",");
			filter=new SingleColumnValueFilter(Bytes.toBytes(familyName),Bytes.toBytes(arr[0]),CompareOp.EQUAL, Bytes.toBytes(arr[1]));
			filterlist.addFilter(filter);
		}
		return filterlist;
	}
	public Map<String, Map<String, String>> getRegionResult(String tableName,byte[] startKey,byte[] endKey,String fieldArr,List<String> keyList){
		Map<String, Map<String, String>> retMap=new HashMap<String, Map<String, String>>();
		ResultScanner scanner=null;
		HConnection conn=null;
		HTableInterface table=null;
		try{
			conn=HConnectionManager.createConnection(cfg);
			table=conn.getTable(tableName);
			Scan scan=new Scan();
			scan.setMaxVersions(1);
			scan.setStartRow(startKey);
			scan.setStopRow(endKey);
			String[] fields=fieldArr.split(",");
			for (int i = 0; i < fields.length; i++) {
				String[] arr=fields[i].split(":");
				scan.addColumn(arr[0].getBytes(), arr[1].getBytes());
			}
			scanner=table.getScanner(scan);
			for (Result rs:scanner) {
				String key=new String(rs.getRow());
				Map<String, String> valueMap=new HashMap<String,String>();
				for (Cell val:rs.listCells()) {
					valueMap.put(new String(val.getQualifierArray()), new String(val.getValueArray()));
				}
				if(keyList!=null) {
                    keyList.add(key);
                }
				retMap.put(key, valueMap);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(scanner!=null){
				scanner.close();
			}
			try{
				if(table!=null){
					table.close();
				}
				if(conn!=null){
					conn.close();
				}
			}catch(Exception ex1){
				
			}
		}
		return retMap;
	}
	
	public void truncate(String tableName){
		try {
			HBaseAdmin admin = new HBaseAdmin(config);
			HTable table=new HTable(config, tableName);
			byte[][] regionKeys=table.getStartKeys();
			HTableDescriptor ds=admin.getTableDescriptor(Bytes.toBytes(tableName));
			admin.disableTable(Bytes.toBytes(tableName));
			admin.deleteTable(Bytes.toBytes(tableName));
			admin.createTable(ds,regionKeys);
			admin.close();
			table.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static KeyValue createKeyValue(String family,String key,String colName,String value)
    {
            return new KeyValue(Bytes.toBytes(key),Bytes.toBytes(family),Bytes.toBytes(colName),
                            System.currentTimeMillis(), Bytes.toBytes(value));
    }

}
