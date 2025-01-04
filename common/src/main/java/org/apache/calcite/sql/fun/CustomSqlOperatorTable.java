package org.apache.calcite.sql.fun;

import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.util.ReflectiveSqlOperatorTable;

public class CustomSqlOperatorTable extends ReflectiveSqlOperatorTable {
    private static CustomSqlOperatorTable instance;

    @Deprecated
    public static final SqlFunction DECODE;
    /**
     * @deprecated
     */
    @Deprecated
    public static final SqlFunction NVL;
    /**
     * @deprecated
     */
    @Deprecated
    public static final SqlFunction LTRIM;
    /**
     * @deprecated
     */
    @Deprecated
    public static final SqlFunction RTRIM;
    /**
     * @deprecated
     */
    @Deprecated
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
