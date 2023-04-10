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

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class ResultSetOperationExtractor implements ResultSetExtractor<Integer> {
    protected String dateFormat = "yyyy-MM-dd";
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss";
    protected String encode = "UTF-8";
    protected LobHandler lobHandler;

    public ResultSetOperationExtractor() {
        init();
    }

    public ResultSetOperationExtractor(String dateFormat, String timestampFormat) {
        if (dateFormat != null) {
            this.dateFormat = dateFormat;
        }
        if (timestampFormat != null) {
            this.timestampFormat = timestampFormat;
        }
    }

    @Override
    public Integer extractData(@NonNull ResultSet rs) throws SQLException,
            DataAccessException {

        Integer retVal = 0;
        Assert.notNull(rs,"resultSet must not be null!");
        Map<String, Object> map = new HashMap<>();
        while (rs.next()) {
            map.clear();
            ResultSetExtractorUtils.wrapResultSetToMap(rs,encode,map);
            if (executeAdditionalOperation(map, rs.getMetaData())) {
                retVal++;
            }
        }
        return retVal;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    /**
     *
     * @param map this Param is reused and Singleton, Don not add this Map to Collections
     * @param rsmd  resulstsetmetadata
     * @return
     * @throws SQLException
     */
    public abstract boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd) throws SQLException;

    public void init(){

    }
}
