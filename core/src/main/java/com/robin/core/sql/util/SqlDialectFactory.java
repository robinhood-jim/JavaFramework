package com.robin.core.sql.util;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.MissingConfigException;


public class SqlDialectFactory {
    public static final BaseSqlGen getSqlGeneratorByDialect(String dbType) throws MissingConfigException {
        BaseSqlGen sqlGen=null;
        if(BaseDataBaseMeta.TYPE_MYSQL.equalsIgnoreCase(dbType)){
            sqlGen=new MysqlSqlGen();
        }else if(BaseDataBaseMeta.TYPE_ORACLE.equalsIgnoreCase(dbType) || BaseDataBaseMeta.TYPE_ORACLERAC.equalsIgnoreCase(dbType)){
            sqlGen=new OracleSqlGen();
        }else if(BaseDataBaseMeta.TYPE_DB2.equalsIgnoreCase(dbType)){
            sqlGen=new DB2SqlGen();
        }else if(BaseDataBaseMeta.TYPE_SQLSERVER.equalsIgnoreCase(dbType)){
            sqlGen=new SqlServer2005Gen();
        }else if(BaseDataBaseMeta.TYPE_SYBASE.equalsIgnoreCase(dbType)){
            sqlGen=new SybaseSqlGen();
        }else if(BaseDataBaseMeta.TYPE_PGSQL.equalsIgnoreCase(dbType)){
            sqlGen=new PostgreSqlSqlGen();
        }else {
            throw new MissingConfigException("unknow db dialect Type "+dbType);
        }
        return sqlGen;
    }
}
