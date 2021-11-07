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
package com.robin.core.sql.util;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import org.springframework.util.StringUtils;


public class Hive2SqlGen extends AbstractSqlGen implements BaseSqlGen{
    private static Hive2SqlGen sqlGen=new Hive2SqlGen();
    private Hive2SqlGen(){

    }
    public static Hive2SqlGen getInstance(){
        return sqlGen;
    }

    @Override
    public String generateSingleRowSql(String querySql) {
        String str = querySql.trim().toLowerCase();
        str=str.replaceAll("\\n", "");
        str=str.replaceAll("\\r", "");
        int nOrderPos = str.lastIndexOf(" order by ");
        if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(str, 0, nOrderPos).append(" limit 1");
        return strBuf.toString();
    }

    @Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {
        if(pageQuery.getPageSize()!=0) {
            Integer[] startEnd = getStartEndRecord(pageQuery);

            strSQL = strSQL.trim();
            StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
            pagingSelect.append("select * from ( select r.*,row_number() over(");
            if(!StringUtils.isEmpty(pageQuery.getOrderString())){
                pagingSelect.append(" order by ").append(pageQuery.getOrderString()).append(") as rownum");
            }
            else if(!StringUtils.isEmpty(pageQuery.getOrder())){
                pagingSelect.append(" order by ").append(pageQuery.getOrder()).append(" ").append(Const.ASC.equalsIgnoreCase(pageQuery.getOrderDirection())?"asc":"desc").append(") as rownum");
            }else{
                pagingSelect.append(") as rownum");
            }
            pagingSelect.append(" from ( ");
            pagingSelect.append(strSQL);
            pagingSelect.append(" )r) r where rownum between ").append(startEnd[0]+1).append(" and ").append(startEnd[1]);
            log.info("pageSql=" + pagingSelect.toString());
            return pagingSelect.toString();
        }else {
            return getNoPageSql(strSQL,pageQuery);
        }
    }

    @Override
    public String getSequenceScript(String sequnceName) throws DAOException {
        throw new DAOException("sequence not support in MySql");
    }

    @Override
    public boolean supportIncrement() throws DAOException {
        throw new DAOException("increment not support in MySql");
    }

    @Override
    public String getDbType() {
        return BaseDataBaseMeta.TYPE_HIVE2;
    }
}
