package com.robin.comm.util.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

@ThreadSafe
public class JedisClientFactory {
    private static class JedisClientHolder {
        public static JedisClient client = new JedisClient();
    }

    public static JedisClient getInstance() {
        return JedisClientHolder.client;
    }

    public static class JedisClient {

        private JedisPool pool = null;

        public int getDbindex() {
            return dbindex;
        }

        public void setDbindex(int dbindex) {
            this.dbindex = dbindex;
        }

        private int dbindex = 0;
        private Gson gson = GsonUtil.getGson();

        private JedisClient() {

        }

        public void init(String propertiesName) {
            ResourceBundle bundle = ResourceBundle.getBundle(propertiesName);
            String ip = bundle.getString("IPADDRESS");
            int port = Integer.parseInt(bundle.getString("PORT"));
            if(bundle.containsKey("DBIDX")){
                dbindex=Integer.parseInt(bundle.getString("DBIDX"));
            }
            String passwd = null;
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(100);
            config.setMaxIdle(20);
            config.setMaxWaitMillis(100000L);
            if (StringUtils.isEmpty(passwd)) {
                pool = new JedisPool(config, ip, port, 100000);
            } else {
                pool = new JedisPool(config, ip, port, 100000, passwd);
            }
        }

        public void putValue(String key, Object obj, Integer expireSecond) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            putValue(jedis, key, obj, expireSecond);
        }

        private void putValue(Jedis jedis, String key, Object obj, Integer expireSecond) {
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                Map<String, String> map1 = new HashMap<String, String>();
                Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Object> entry = iter.next();
                    String valstr = "";
                    if (null != entry.getValue()) {
                        valstr = gson.toJson(entry.getValue());
                    }
                    map1.put(entry.getKey(), valstr);
                }
                jedis.hmset(key, map1);
            } else if (obj instanceof List) {
                List list = (List) obj;
                for (Object str : list) {
                    if (str instanceof String) {
                        jedis.lpush(key, str.toString());
                    } else {
                        String valstr = gson.toJson(str);
                        jedis.lpush(key, valstr);
                    }
                }
            } else if (obj instanceof String) {
                jedis.set(key, obj.toString());
            } else {
                jedis.set(key, obj.toString());
            }
            if (expireSecond != null && !expireSecond.equals(-1)) {
                jedis.expire(key, expireSecond);
            }
            close(jedis);
        }

        public void putValue(String key, Object obj, int dbIndex, Integer expireSecond) {
            Jedis jedis = pool.getResource();
            jedis.select(dbIndex);
            putValue(jedis, key, obj, expireSecond);
        }


        public void putSerializableObject(String key, Object value,
                                          Integer expireSecond) throws IOException {
            try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
                Jedis jedis = getJedis();
                jedis.set(key.getBytes(), arrayOutputStream.toByteArray());
                if (expireSecond != null && !expireSecond.equals(-1)) {
                    jedis.expire(key, expireSecond);
                }
                close(jedis);
            }
        }

        public Object getSerializableObject(String key, Class<?> clazz) throws IOException,ClassNotFoundException {
            Jedis jedis = getJedis();
            try (ByteArrayInputStream in = new ByteArrayInputStream(jedis.get(key.getBytes()));
                    ObjectInputStream arrayin = new ObjectInputStream(in)) {
                Object obj = null;
                Object tmpobj = arrayin.readObject();
                close(jedis);
                obj = clazz.cast(tmpobj);
                return obj;
            }
        }

        public void putPlainSet(String key, String... value) {
            Jedis jedis = getJedis();
            jedis.sadd(key, value);
            close(jedis);
        }

        public byte[] putSetWithSchema(Schema schema, Schema nestedSchema, List<? extends Serializable> valueObject) throws Exception {
            Assert.notEmpty(valueObject, "array is null");
            Map<String, Method> getMethods = ReflectUtils.returnGetMethods(valueObject.get(0).getClass());

            try {
                GenericRecord rd = new GenericData.Record(nestedSchema);
                List<GenericRecord> retList = new ArrayList<>();
                for (Serializable obj : valueObject) {
                    GenericRecord genericRecord = new GenericData.Record(schema);
                    Iterator<Map.Entry<String, Method>> iter = getMethods.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, Method> entry = iter.next();
                        genericRecord.put(entry.getKey(), AvroUtils.acquireGenericRecord(entry.getKey(), entry.getValue().invoke(obj, null), schema));
                    }
                    retList.add(genericRecord);
                }
                rd.put("list", retList);
                byte[] bytes = AvroUtils.dataToByteArray(nestedSchema, rd);
                byte[] bytes1 = AvroUtils.dataToByteWithBijection(nestedSchema, rd);
                System.out.println(bytes.length);
                System.out.println(bytes1.length);
                GenericRecord record = AvroUtils.parse(nestedSchema, bytes);
                System.out.println(record);
                return bytes;
                //getJedis().sadd(key.getBytes(),AvroUtils.dataToByteWithBijection(nestedSchema,))
            } catch (Exception ex) {
                throw ex;
            }
        }

        private Jedis getJedis() {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            return jedis;
        }

        public void putPlainSet(String key, List<?> valueList) throws IOException {
            try(Jedis jedis = pool.getResource()) {
                if (dbindex != 0) {
                    jedis.select(dbindex);
                }
                byte[][] byteArr = new byte[valueList.size()][];
                for (int i = 0; i < valueList.size(); i++) {
                    try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                         ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
                        outputStream.writeObject(valueList.get(i));
                        byteArr[i] = arrayOutputStream.toByteArray();
                    }
                }
                jedis.sadd(key.getBytes(), byteArr);
            }
        }

        public void rmPlainSet(String key, String value) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            jedis.srem(key, value);
            close(jedis);
        }

        public Set<String> getPlainSet(String key) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            Set<String> retSet = jedis.smembers(key);
            close(jedis);
            return retSet;
        }

        public List getPlainSetWithObj(String key, Class<?> clazz) throws IOException,ClassNotFoundException {
            List list = new ArrayList();
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            Set<byte[]> retSet = jedis.smembers(key.getBytes());
            Iterator<byte[]> it = retSet.iterator();
            while (it.hasNext()) {
                try(ByteArrayInputStream in = new ByteArrayInputStream(jedis.get(key.getBytes()));
                    ObjectInputStream arrayin = new ObjectInputStream(in)) {
                    Object tmpobj = arrayin.readObject();
                    list.add(clazz.cast(tmpobj));
                }
            }
            close(jedis);
            return list;
        }

        public boolean isKeyExists(String key) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            boolean isExists = jedis.exists(key);
            String val = jedis.get(key);
            if (val == null || "".equals(val)) {
                isExists = false;
            }
            close(jedis);
            return isExists;
        }

        public void clearValue(String key) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            jedis.del(key);
            close(jedis);
        }

        public String getPlainValue(String key) {
            Jedis jedis = pool.getResource();
            if (dbindex != 0) {
                jedis.select(dbindex);
            }
            String str = jedis.get(key);
            close(jedis);
            return str;
        }

        public String getPlainValue(String key, int dbIndex) {
            Jedis jedis = pool.getResource();
            jedis.select(dbIndex);
            String str = jedis.get(key);
            close(jedis);
            return str;
        }

        public String flushDB(int dbIndex) {
            Jedis jedis = pool.getResource();
            jedis.select(dbIndex);
            String str = jedis.flushDB();
            close(jedis);
            return str;
        }

        public String flushAll() {
            Jedis jedis = pool.getResource();
            String str = jedis.flushAll();
            close(jedis);
            return str;
        }


        public void lpush(String key, String value) {
            Jedis jedis = pool.getResource();
            jedis.lpush(key, value);
            close(jedis);
        }

        public void rpush(String key, String value) {
            Jedis jedis = pool.getResource();
            jedis.rpush(key, value);
            close(jedis);
        }

        public void hmset(String key, Map<String, ?> map) {
            Jedis jedis = pool.getResource();
            try {
                Iterator<?> iterator = map.entrySet().iterator();
                Object tmpobj = null;
                if (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    tmpobj = entry.getValue();
                }
                if (tmpobj instanceof String) {
                    jedis.hmset(key, (Map<String, String>) map);
                } else if (tmpobj != null && isWrapClass(tmpobj.getClass())) {
                    Iterator<? extends Map.Entry<String, ?>> it = map.entrySet().iterator();
                    Map<String, String> map1 = new HashMap<>();
                    while (it.hasNext()) {
                        Map.Entry entry=it.next();
                        String key1 = entry.getKey().toString();
                        if(entry.getValue().getClass().isAssignableFrom(String.class)) {
                            map1.put(key1, entry.getValue().toString());
                        }else{
                            map1.put(key1,gson.toJson(entry.getValue()));
                        }
                    }
                    jedis.hmset(key, map1);
                } else {
                    Iterator<? extends Map.Entry<String, ?>> it = map.entrySet().iterator();
                    Map<byte[], byte[]> tmpmap = new HashMap<byte[], byte[]>();
                    while (it.hasNext()) {
                        Map.Entry<String,?> entry=it.next();
                        String key1 = entry.getKey();
                        byte[] tmpbyte = SerializeObject(entry.getValue());
                        if (tmpbyte != null) {
                            tmpmap.put(key1.getBytes(), tmpbyte);
                        }
                    }
                    jedis.hmset(key.getBytes(), tmpmap);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                close(jedis);
            }
        }

        public List<?> hmget(String key, String[] fields, Class targetClass) throws IOException {
            Jedis jedis = pool.getResource();
            try {
                Object tmpobj = targetClass.newInstance();

                if (tmpobj instanceof String) {
                    List<String> list1 = jedis.hmget(key, fields);
                    return list1;
                } else if (isWrapClass(targetClass)) {
                    List<String> list1 = jedis.hmget(key, fields);
                    List retList = new ArrayList();
                    Method method = targetClass.getDeclaredMethod("valueOf", String.class);
                    for (int i = 0; i < list1.size(); i++) {
                        retList.add(method.invoke(tmpobj, list1.get(i)));
                    }
                    return retList;
                } else {
                    List retList = new ArrayList();
                    byte[][] bt1 = new byte[fields.length][];
                    for (int i = 0; i < fields.length; i++) {
                        bt1[i] = fields[i].getBytes();
                    }
                    List<byte[]> list2 = jedis.hmget(key.getBytes(), bt1);
                    for (int i = 0; i < list2.size(); i++) {
                        retList.add(DeSerializeObject(list2.get(i)));
                    }
                    return list2;
                }
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

        public void lpush(String key, List<?> valueList) throws IOException {
            Jedis jedis = pool.getResource();
            for (Object value : valueList) {
                if (value instanceof String) {
                    jedis.lpush(key, value.toString());
                } else if (isWrapClass(value.getClass())) {
                    jedis.lpush(key, value.toString());
                } else {
                    try( ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                         ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
                        outputStream.writeObject(value);
                        jedis.lpush(key.getBytes(), arrayOutputStream.toByteArray());
                    }
                }
            }
            close(jedis);
        }

        public void rpush(String key, List<?> valueList) throws IOException{
            Jedis jedis = pool.getResource();
            for (Object value : valueList) {
                if (value instanceof String) {
                    jedis.lpush(key, value.toString());
                } else if (isWrapClass(value.getClass())) {
                    jedis.lpush(key, value.toString());
                } else {
                    try(ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
                        outputStream.writeObject(value);
                        jedis.rpush(key.getBytes(), arrayOutputStream.toByteArray());
                    }
                }
            }
            close(jedis);
        }

        public List<String> lrange(String key, int start, int end) {
            Jedis jedis = pool.getResource();
            if (end == 0) {
                end = -1;
            }
            List<String> retList = jedis.lrange(key, start, end);
            close(jedis);
            return retList;
        }

        public List<Object> lrangeEntity(String key, int start, int end) {
            Jedis jedis = pool.getResource();
            List<Object> list = new ArrayList<Object>();
            if (end == 0) {
                end = -1;
            }
            List<byte[]> retList = jedis.lrange(key.getBytes(), start, end);
            for (byte[] str : retList) {
                try( ByteArrayInputStream in = new ByteArrayInputStream(str);
                     ObjectInputStream arrayin = new ObjectInputStream(in)) {
                    Object tmpobj = arrayin.readObject();
                    list.add(tmpobj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            close(jedis);
            return list;
        }

        public String lpop(String key) {
            Jedis jedis = pool.getResource();
            String retStr = jedis.lpop(key);
            close(jedis);
            return retStr;
        }

        public void incr(String key) {
            Jedis jedis = pool.getResource();
            jedis.incr(key);
            close(jedis);
        }

        public void incrBy(String key, Long value) {
            Jedis jedis = pool.getResource();
            jedis.incrBy(key, value);
            close(jedis);
        }

        public void decr(String key) {
            Jedis jedis = pool.getResource();
            jedis.decr(key);
            close(jedis);
        }

        public void decrBy(String key, Long value) {
            Jedis jedis = pool.getResource();
            jedis.decrBy(key, value);
            close(jedis);
        }


        public Object getValue(String key,
                               Class clazz, Class... targetClassArr) {
            Object retObj = null;
            Jedis jedis = pool.getResource();
            try {
                if (clazz.getClass().isAssignableFrom(Map.class)) {
                    Map<String, String> map = jedis.hgetAll(key);
                    retObj = map;
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String,String> entry=iter.next();
                        String keycol = entry.getKey();
                        String valStr = entry.getValue();
                        if (valStr.startsWith("[")) {
                            // list
                            List<Map<String, Object>> tlist = gson.fromJson(valStr, new TypeToken<List<Map<String, Object>>>() {
                            }.getType());
                            map1.put(keycol, tlist);
                        } else if (valStr.startsWith("{")) {
                            // Object
                            Map<String, Object> tmap = gson.fromJson(valStr, new TypeToken<Map<String, Object>>() {
                            }.getType());
                            map1.put(keycol, tmap);
                        }
                        retObj = map1;
                    }

                } else if (clazz.isAssignableFrom(List.class)) {
                    List<Object> objList = new ArrayList<Object>();
                    List<String> listobj = jedis.lrange(key, 0, -1);
                    for (String str : listobj) {
                        if (targetClassArr.length > 0) {
                            if (str.startsWith("[")) {
                                List<?> list = gson.fromJson(str, TypeToken.getParameterized(ArrayList.class, targetClassArr[0]).getType());
                                retObj = list;
                            } else if (str.startsWith("{")) {
                                retObj = gson.fromJson(str, TypeToken.get(targetClassArr[0]).getType());
                            }
                        } else {
                            if (str.startsWith("[")) {
                                List<Map<String, Object>> list = gson.fromJson(str, new TypeToken<List<Map<String, Object>>>() {
                                }.getType());
                                retObj = list;
                            } else if (str.startsWith("{")) {
                                retObj = gson.fromJson(str, new TypeToken<Map<String, Object>>() {
                                }.getType());
                            }
                        }
                    }
                } else if (clazz.isAssignableFrom(String.class)) {
                    retObj = jedis.get(key);
                } else {
                    String val = jedis.get(key);
                    retObj = gson.fromJson(val, TypeToken.get(clazz).getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close(jedis);
            return retObj;
        }


        private byte[] SerializeObject(Object obj) {
            try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)){
                outputStream.writeObject(obj);
                byte[] byteArr = arrayOutputStream.toByteArray();
                return byteArr;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        private Object DeSerializeObject(byte[] bytes) throws IOException,ClassNotFoundException {
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                 ObjectInputStream arrayin = new ObjectInputStream(in)){
                Object tmpobj = arrayin.readObject();
                return tmpobj;
            }
        }

        public static boolean isWrapClass(Class clz) {
            try {
                return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
            } catch (Exception e) {
                return false;
            }
        }

        private void close(Jedis jedis) {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
