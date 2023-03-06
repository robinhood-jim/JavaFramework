/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.query.extractor;

import com.robin.core.base.dao.util.AnnotationRetriever;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.ObjectUtils;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitPageResultSetExtractor implements ResultSetExtractor<List<Map<String, Object>>> {
    private final int start;

    private final int len;
    private LobHandler lobHandler;
    private List<AnnotationRetriever.FieldContent> mappingFieldList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public SplitPageResultSetExtractor(int start, int len) {
        this.start = start;
        this.len = len;
    }

    public SplitPageResultSetExtractor(int start, int len, LobHandler handler, List<AnnotationRetriever.FieldContent> mappingFieldList) {

        this.start = start;
        this.len = len;
        this.lobHandler = handler;
        this.mappingFieldList = mappingFieldList;
    }

    public SplitPageResultSetExtractor(int start, int len, LobHandler handler) {

        this.start = start;
        this.len = len;
        this.lobHandler = handler;
    }
    public SplitPageResultSetExtractor(int start,int len,LobHandler handler,String dayFormatter,String timestampFormatter){
        this.start = start;
        this.len = len;
        this.lobHandler = handler;
        this.dateFormatter=DateTimeFormatter.ofPattern(dayFormatter);
        this.timestampFormatter=DateTimeFormatter.ofPattern(timestampFormatter);
    }

    @Override
    public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException,
            DataAccessException {
        return wrapMapper(rs, start, len);
    }

    private List<Map<String, Object>> wrapMapper(ResultSet rs, int start, int len) throws SQLException, DataAccessException {
        int end = start + len;
        boolean allcode = false;
        if (end == 0) {
            allcode = true;
        }
        int rowNum = 0;
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        String[] columnName = new String[count];
        String[] className = new String[count];
        Integer[] types = new Integer[count];
        int colpos = 0;
        if (mappingFieldList != null) {
            for (AnnotationRetriever.FieldContent fieldContent : mappingFieldList) {
                if (fieldContent.isPrimary()) {
                    if (fieldContent.getPrimaryKeys() != null) {
                        for (AnnotationRetriever.FieldContent fieldContent1 : fieldContent.getPrimaryKeys()) {
                            assignVal(fieldContent1, rsmd, columnName, types, className, colpos);
                            colpos++;
                        }
                    } else {
                        assignVal(fieldContent, rsmd, columnName, types, className, colpos);
                        colpos++;
                    }
                } else {
                    assignVal(fieldContent, rsmd, columnName, types, className, colpos);
                    colpos++;
                }
            }
        } else {
            for (int k = 0; k < count; k++) {
                columnName[k] = rsmd.getColumnLabel(k + 1);
                types[k] = rsmd.getColumnType(k + 1);
                String fullclassName = rsmd.getColumnClassName(k + 1);
                int pos = fullclassName.lastIndexOf(".");
                className[k] = fullclassName.substring(pos + 1).toUpperCase();
            }
        }
        while (rs.next()) {
            ++rowNum;
            if (!allcode) {
                if (rowNum <= start) {
                    continue;
                } else if (rowNum > end) {
                    break;
                }
            }
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < count; i++) {
                rs.getObject(i + 1);
                if (rs.wasNull()) {
                    map.put(columnName[i], "");
                }
                switch (types[i]) {
                    case Types.DATE:
                        Date date = rs.getDate(i + 1);
                        if (!ObjectUtils.isEmpty(date)) {
                            map.put(columnName[i], dateFormatter.format(date.toLocalDate().atStartOfDay()));
                        }
                        break;
                    case Types.TIMESTAMP:
                        Timestamp stamp = rs.getTimestamp(i + 1);
                        if (!ObjectUtils.isEmpty(stamp)) {
                            map.put(columnName[i], timestampFormatter.format(stamp.toLocalDateTime()));
                        }
                        break;
                    case Types.CLOB:
                    case Types.NCLOB:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                        if (lobHandler != null) {
                            String result = lobHandler.getClobAsString(rs, i + 1);
                            map.put(columnName[i], result);
                        }
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.LONGVARBINARY:
                        if (lobHandler != null) {
                            byte[] bytes = lobHandler.getBlobAsBytes(rs, i + 1);
                            map.put(columnName[i], bytes);
                        }
                        break;
                    default:
                        if (!ObjectUtils.isEmpty(rs.getObject(i + 1))) {
                            map.put(columnName[i], rs.getObject(i + 1).toString().trim());
                        }
                }
            }
            list.add(map);
        }
        return list;
    }

    private void assignVal(AnnotationRetriever.FieldContent fieldContent, ResultSetMetaData rsmd, String[] columnName, Integer[] types, String[] className, int colpos) throws SQLException {
        columnName[colpos] = fieldContent.getPropertyName();
        types[colpos] = rsmd.getColumnType(colpos + 1);
        String fullclassName = rsmd.getColumnClassName(colpos + 1);
        int pos = fullclassName.lastIndexOf(".");
        className[colpos] = fullclassName.substring(pos + 1).toUpperCase();

    }



}
