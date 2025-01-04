package org.apache.calcite.sql.fun;

import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.util.ReflectiveSqlOperatorTable;

public class CustomSqlOperatorTable extends ReflectiveSqlOperatorTable {
    private static CustomSqlOperatorTable instance;


    public static final SqlFunction DECODE;

    public static final SqlFunction NVL;

    public static final SqlFunction LTRIM;

    public static final SqlFunction RTRIM;

    public static final SqlFunction SUBSTR;
    public static final SqlFunction IFNULL;

    public CustomSqlOperatorTable() {
    }

    public static synchronized CustomSqlOperatorTable instance() {
        if (instance == null) {
            instance = new CustomSqlOperatorTable();
            instance.init();
        }
        return instance;
    }

    static {
        DECODE = SqlLibraryOperators.DECODE;
        NVL = SqlLibraryOperators.NVL;
        LTRIM = SqlLibraryOperators.LTRIM;
        RTRIM = SqlLibraryOperators.RTRIM;
        SUBSTR = SqlLibraryOperators.SUBSTR_MYSQL;
        IFNULL = SqlLibraryOperators.IFNULL;
    }
}
