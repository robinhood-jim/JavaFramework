package com.robin.core.sql.util;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.MissingConfigException;


public class SqlDialectFactory {
    public static final BaseSqlGen getSqlGeneratorByDialect(String dbType) throws MissingConfigException {
        BaseSqlGen sqlGen=null;
        if(BaseDataBaseMeta.TYPE_MYSQL.equalsIgnoreCase(dbType)){
            sqlGen=MysqlSqlGen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_ORACLE.equalsIgnoreCase(dbType) || BaseDataBaseMeta.TYPE_ORACLERAC.equalsIgnoreCase(dbType)){
            sqlGen=OracleSqlGen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_DB2.equalsIgnoreCase(dbType)){
            sqlGen=DB2SqlGen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_SQLSERVER.equalsIgnoreCase(dbType)){
            sqlGen=SqlServer2005Gen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_SYBASE.equalsIgnoreCase(dbType)){
            sqlGen=SybaseSqlGen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_PGSQL.equalsIgnoreCase(dbType)){
            sqlGen=PostgreSqlSqlGen.getInstance();
        }else if(BaseDataBaseMeta.TYPE_HIVE2.equalsIgnoreCase(dbType)){
            sqlGen=Hive2SqlGen.getInstance();
        }
        else {
            throw new MissingConfigException("unknow db dialect Type "+dbType);
        }
        return sqlGen;
    }
}
