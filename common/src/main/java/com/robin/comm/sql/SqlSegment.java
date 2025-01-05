package com.robin.comm.sql;

import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.Data;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
public class SqlSegment {
    private List<CommSqlParser.ValueParts> selectColumns;
    private String tableName;
    private String tabAlias;
    private SqlNode whereCause;
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
    private List<CommSqlParser.ValueParts> having;
    private Map<String,DataSetColumnMeta> originSchemaMap;
}
