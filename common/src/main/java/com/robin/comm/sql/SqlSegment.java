package com.robin.comm.sql;

import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.Data;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
public class SqlSegment {
    private List<CommSqlParser.ValueParts> selectColumns;
    private String tableName;
    private String tabAlias;
    private SqlNode whereCause;
    private SqlNode havingCause;
    private Map<Integer,Integer> newColumnPosMap;
    private List<CommSqlParser.ValueParts> whereColumns=new ArrayList<>();
    private String newColumnPrefix;
    private List<DataSetColumnMeta> calculateSchema;
    private boolean includeAllOriginColumn=false;

    private Map<String, Set<String>> inPartMap = new HashMap<>();
    private Map<String, Pair<Double, Double>> rangeMap = new HashMap<>();
    private Map<String, CommSqlParser.ValueParts> wherePartsMap;
    private Map<String, CommSqlParser.ValueParts> selectPartsMap;
    private List<SqlNode> groupBy;
    private List<CommSqlParser.ValueParts> having=new ArrayList<>();
    private Map<String,DataSetColumnMeta> originSchemaMap;
    // if filterSql has four operations,orc and parquet can not use filter directly
    private boolean selectHasFourOperations = false;
    private boolean conditionHasFourOperations=false;
    private boolean conditionHasFunction=false;
    // if filterSql compare right hand is column,orc and parquet can not use filter directly
    private boolean hasRightColumnCmp = false;
    private List<Pair<SqlNode,Boolean>> orderBys=new ArrayList<>();
}
