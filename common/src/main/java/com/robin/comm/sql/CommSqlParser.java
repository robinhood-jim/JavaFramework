package com.robin.comm.sql;


import com.google.common.collect.Sets;
import com.robin.core.base.exception.GenericException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.Lex;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.CustomSqlOperatorTable;
import org.apache.calcite.sql.fun.SqlCase;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.ValidationException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CommSqlParser {
    private static final SchemaPlus rootSchema;

    private CommSqlParser() {

    }

    static {
        rootSchema = Frameworks.createRootSchema(true);
    }

    public static SqlSegment parseGroupByAgg(String sql, Lex lex, DataCollectionMeta meta, String newColumnPrefix) {
        SqlSegment segment = new SqlSegment();
        String tabName = null;
        String tabAlias = null;
        try {
            SqlSelect sqlSelect = parseWithLex(sql, lex, meta);
            if (SqlKind.AS.equals(sqlSelect.getFrom().getKind())) {
                List<SqlNode> tableNode = ((SqlBasicCall) sqlSelect.getFrom()).getOperandList();
                tabName = tableNode.get(0).toString();
                tabAlias = tableNode.get(1).toString();
            } else {
                tabName = sqlSelect.getFrom().toString();
            }
            segment.setTableName(tabName);
            segment.setTabAlias(tabAlias);
            SqlNodeList selectLists = sqlSelect.getSelectList();
            Map<Integer, Integer> newColumnPosMap = new HashMap<>();
            List<ValueParts> columns = parseSelectColumn(segment, selectLists, newColumnPrefix, newColumnPosMap);
            List<SqlNode> groupNodes = sqlSelect.getGroup();

            segment.setGroupBy(groupNodes);
            segment.setSelectColumns(columns);
            segment.setWhereCause(sqlSelect.getWhere());
            segment.setNewColumnPosMap(newColumnPosMap);
            parseWhereParts(segment, segment.getWhereCause(), segment.getWhereColumns());
            SqlNode havingNode = sqlSelect.getHaving();
            segment.setHavingCause(havingNode);
            parseWhereParts(segment, havingNode, segment.getHaving());
            return segment;
        }catch (SqlParseException|ValidationException ex){
            throw new GenericException(ex);
        }
    }

    public static SqlSegment parseSingleTableQuerySql(String sql, Lex lex, DataCollectionMeta meta, String newColumnPrefix) {
        SqlSegment segment = new SqlSegment();
        String tabName = null;
        String tabAlias = null;

        try {
            SqlSelect sqlSelect = parseWithLex(sql, lex, meta);
            if (SqlKind.AS.equals(sqlSelect.getFrom().getKind())) {
                List<SqlNode> tableNode = ((SqlBasicCall) sqlSelect.getFrom()).getOperandList();
                tabName = tableNode.get(0).toString();
                tabAlias = tableNode.get(1).toString();
            } else {
                tabName = sqlSelect.getFrom().toString();
            }
            segment.setTableName(tabName);
            segment.setTabAlias(tabAlias);
            segment.setNewColumnPrefix(newColumnPrefix);
            SqlNodeList selectLists = sqlSelect.getSelectList();
            Map<Integer, Integer> newColumnPosMap = new HashMap<>();
            List<ValueParts> columns = parseSelectColumn(segment, selectLists, newColumnPrefix, newColumnPosMap);
            segment.setSelectColumns(columns);
            segment.setWhereCause(sqlSelect.getWhere());
            segment.setNewColumnPosMap(newColumnPosMap);
            parseWhereParts(segment, segment.getWhereCause(),segment.getWhereColumns());
            List<DataSetColumnMeta> calculateSchema = getCalculateSchema(segment, meta);
            segment.setCalculateSchema(calculateSchema);
            segment.setOriginSchemaMap(meta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnName, Function.identity())));
            return segment;
        } catch (SqlParseException | ValidationException ex) {
            throw new GenericException(ex);
        }
    }

    private static SqlSelect parseWithLex(String sql, Lex lex, DataCollectionMeta meta) throws SqlParseException, ValidationException {
        DataMetaCalciteTable table = new DataMetaCalciteTable(meta);
        rootSchema.add(meta.getTableName(), table);
        FrameworkConfig workConfig = Frameworks.newConfigBuilder().defaultSchema(rootSchema).parserConfig(SqlParser.configBuilder().setLex(lex).build()).operatorTable(SqlOperatorTables.chain(SqlStdOperatorTable.instance(), CustomSqlOperatorTable.instance())).build();
        Planner planner = Frameworks.getPlanner(workConfig);

        SqlNode tNodes = planner.parse(sql);
        planner.validate(tNodes);
        SqlParser.Config config = SqlParser.configBuilder().setLex(lex).build();
        SqlParser parser = SqlParser.create(sql, config);
        SqlNode sqlNode = parser.parseQuery();
        Assert.isTrue(SqlKind.SELECT.equals(sqlNode.getKind()), "can only parse select Sql");
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        Assert.isTrue(!SqlKind.JOIN.equals(sqlSelect.getFrom().getKind()), "only support single table query");
        return sqlSelect;
    }

    public static List<DataSetColumnMeta> getCalculateSchema(SqlSegment segment, DataCollectionMeta meta) {
        Map<String, DataSetColumnMeta> columnMetaMap = meta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnName, Function.identity()));
        List<DataSetColumnMeta> columns = new ArrayList<>();
        if (!CollectionUtils.isEmpty(segment.getSelectColumns())) {
            for (ValueParts parts : segment.getSelectColumns()) {
                String columnName = parts.aliasName;
                if (!ObjectUtils.isEmpty(parts.getIdentifyColumn())) {
                    DataSetColumnMeta meta1 = null;
                    if (columnMetaMap.containsKey(parts.getIdentifyColumn())) {
                        meta1 = columnMetaMap.get(parts.getIdentifyColumn());
                    } else if (columnMetaMap.containsKey(parts.getIdentifyColumn().toUpperCase())) {
                        meta1 = columnMetaMap.get(parts.getIdentifyColumn().toUpperCase());
                    }
                    columns.add(meta1);
                } else {
                    columns.add(new DataSetColumnMeta(columnName, Const.META_TYPE_FORMULA));
                }
            }
        }
        return columns;
    }

    private static List<ValueParts> parseSelectColumn(SqlSegment segment, SqlNodeList selectLists, String newColumnPrefix, Map<Integer, Integer> newColumnPosMap) {
        List<ValueParts> selectColumns = new ArrayList<>();
        for (SqlNode selected : selectLists) {
            ValueParts valueParts = new ValueParts();
            if (SqlKind.ALL.equals(selected.getKind())) {
                segment.setIncludeAllOriginColumn(true);
            } else if (SqlKind.IDENTIFIER.equals(selected.getKind())) {
                valueParts.setIdentifyColumn(selected.toString());
            } else if (SqlKind.AS.equals(selected.getKind())) {
                List<SqlNode> columnNodes = ((SqlBasicCall) selected).getOperandList();
                valueParts.setAliasName(columnNodes.get(1).toString());
                if (SqlKind.IDENTIFIER.equals(columnNodes.get(0).getKind())) {
                    valueParts.setIdentifyColumn(columnNodes.get(0).toString());
                } else if (SqlKind.CASE.equals(columnNodes.get(0).getKind())) {
                    parseCase((SqlCase) columnNodes.get(0), valueParts);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                } else if (SqlKind.FUNCTION.contains(columnNodes.get(0).getKind())) {
                    List<SqlNode> funcNodes = ((SqlBasicCall) columnNodes.get(0)).getOperandList();
                    valueParts.setFunctionName(((SqlBasicCall) columnNodes.get(0)).getOperator().toString());
                    valueParts.setFunctionParams(funcNodes);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                }else if(SqlKind.IN.equals(columnNodes.get(0).getKind()) || SqlKind.NOT_IN.equals(columnNodes.get(0).getKind())){
                    List<SqlNode> sqlNodes = ((SqlBasicCall) columnNodes.get(0)).getOperandList();
                    SqlIdentifier identifier = (SqlIdentifier) sqlNodes.get(0);
                    Set<String> sets = Sets.newHashSet();
                    for (int i = 1; i < sqlNodes.size(); i++) {
                        sets.add(sqlNodes.get(i).toString());
                    }
                    segment.getInPartMap().put(identifier.toString(),sets);
                }
                else if (SqlBasicCall.class.isAssignableFrom(columnNodes.get(0).getClass())) {
                    valueParts.setNodeString(columnNodes.get(0).toString());
                    valueParts.setCalculator(columnNodes.get(0));
                    segment.setSelectHasFourOperations(true);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                }
            } else if (SqlBasicCall.class.isAssignableFrom(selected.getClass())) {
                if (SqlKind.CASE.equals(selected.getKind())) {
                    parseCase((SqlCase) selected, valueParts);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                } else if (SqlKind.FUNCTION.contains(selected.getKind())) {
                    valueParts.setFunctionName(((SqlBasicCall) selected).getOperator().toString());
                    valueParts.setFunctionParams(((SqlBasicCall) selected).getOperandList());
                    valueParts.setNode(selected);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                } else {
                    valueParts.setCalculator(selected);
                    setAliasName(newColumnPrefix, newColumnPosMap, valueParts);
                }
            } else if (SqlLiteral.class.isAssignableFrom(selected.getClass())) {
                valueParts.setConstantValue((SqlLiteral) selected);
            }
            selectColumns.add(valueParts);
        }
        return selectColumns;
    }

    private static void setAliasName(String newColumnPrefix, Map<Integer, Integer> newColumnPosMap, ValueParts valueParts) {
        if (ObjectUtils.isEmpty(valueParts.getAliasName())) {
            valueParts.setAliasName(returnDefaultNewColumn(newColumnPrefix, newColumnPosMap));
        }
    }

    private static String returnDefaultNewColumn(String columnPrefix, Map<Integer, Integer> newColumnPosMap) {
        String newColumnName;
        if (!newColumnPosMap.containsKey(1)) {
            newColumnName = columnPrefix + "1";
            newColumnPosMap.put(1, 2);
        } else {
            newColumnName = columnPrefix + newColumnPosMap.get(1);
            newColumnPosMap.put(1, newColumnPosMap.get(1) + 1);
        }
        return newColumnName;
    }

    private static void parseCase(SqlCase selected, ValueParts valueParts) {
        valueParts.setSqlKind(SqlKind.CASE);
        SqlCase sqlCase = selected;
        if (!CollectionUtils.isEmpty(sqlCase.getOperandList())) {
            Map<String, ValueParts> caseMap = new HashMap<>();
            String cmpColumn = null;
            for (int i = 0; i < sqlCase.getWhenOperands().size(); i++) {
                String whenValue = null;
                SqlNode whenNode = sqlCase.getWhenOperands().get(i);
                SqlNode thenNode = sqlCase.getThenOperands().get(i);

                List<SqlNode> whenParts = ((SqlBasicCall) whenNode).getOperandList();

                ValueParts thenValue = new ValueParts();
                if (cmpColumn == null) {
                    cmpColumn = whenParts.get(0).toString();
                    valueParts.setIdentifyColumn(cmpColumn);
                }
                if (SqlKind.LITERAL.equals(whenParts.get(1).getKind())) {
                    whenValue = ((SqlLiteral) whenParts.get(1)).getValue().toString();
                    valueParts.decideType((SqlLiteral) whenParts.get(1));
                } else if (SqlBasicCall.class.isAssignableFrom(whenParts.get(1).getClass())) {
                    throw new OperationNotSupportException("when can not use formula");
                }
                if (SqlKind.LITERAL.equals(thenNode.getKind())) {
                    thenValue.setConstantValue((SqlLiteral) thenNode);
                    valueParts.decideType((SqlLiteral) thenNode);
                } else if (SqlKind.IDENTIFIER.equals(thenNode.getKind())) {
                    thenValue.setIdentifyColumn(thenNode.toString());
                } else if (SqlBasicCall.class.isAssignableFrom(thenNode.getClass())) {
                    thenValue.setCalculator(thenNode);
                }
                caseMap.put(whenValue, thenValue);
            }
            if (sqlCase.getElseOperand() != null) {
                ValueParts elsePars = new ValueParts();
                if (SqlKind.LITERAL.equals(sqlCase.getElseOperand().getKind())) {
                    elsePars.setConstantValue((SqlLiteral) sqlCase.getElseOperand());
                    valueParts.decideType((SqlLiteral) sqlCase.getElseOperand());
                } else if (SqlKind.IDENTIFIER.equals(sqlCase.getElseOperand().getKind())) {
                    elsePars.setIdentifyColumn(sqlCase.getElseOperand().toString());
                } else if (SqlBasicCall.class.isAssignableFrom(sqlCase.getElseOperand().getClass())) {
                    elsePars.setCalculator(sqlCase.getElseOperand());
                }
                valueParts.setCaseElseParts(elsePars);
            }
            valueParts.setCaseMap(caseMap);
        }
    }

    private static void parseWhereParts(SqlSegment segment, SqlNode whereNode,List<CommSqlParser.ValueParts> newColumns) {

        if (SqlBasicCall.class.isAssignableFrom(whereNode.getClass())) {
            if (SqlKind.OR.equals(whereNode.getKind()) || SqlKind.AND.equals(whereNode.getKind()) || SqlKind.NOT.equals(whereNode.getKind())) {
                List<SqlNode> sqlNodeList = ((SqlBasicCall) whereNode).getOperandList();
                for (SqlNode node : sqlNodeList) {
                    parseWhereParts(segment, node,newColumns);
                }
            } else {
                List<SqlNode> nodes = ((SqlBasicCall) whereNode).getOperandList();
                for (SqlNode node : nodes) {
                    String calculator = node.toString().replace(Quoting.BACK_TICK.string, "");
                    if (FilterSqlParser.fourZeOper.matcher(calculator).find()) {
                        ValueParts parts = new ValueParts();
                        parts.setCalculator(node);
                        parts.setNodeString(node.toString());
                        parts.setAliasName(returnDefaultNewColumn(segment.getNewColumnPrefix(), segment.getNewColumnPosMap()));
                        segment.setConditionHasFourOperations(true);
                        newColumns.add(parts);
                    }else if(SqlKind.FUNCTION.contains(node.getKind())){
                        List<SqlNode> nodes1=((SqlBasicCall)node).getOperandList();
                        ValueParts parts = new ValueParts();
                        parts.setFunctionName(((SqlBasicCall) node).getOperator().getName());
                        parts.setFunctionParams(nodes1);
                        parts.setSqlKind(node.getKind());
                        segment.setConditionHasFunction(true);
                        newColumns.add(parts);
                    }else if(SqlKind.LITERAL.equals(node.getKind())){

                    }
                }
            }
        } else {
            String calculator = whereNode.toString().replace(Quoting.BACK_TICK.string, "");
            if (FilterSqlParser.fourZeOper.matcher(calculator).find()) {
                ValueParts parts = new ValueParts();
                parts.setCalculator(whereNode);
                parts.setAliasName(returnDefaultNewColumn(segment.getNewColumnPrefix(), segment.getNewColumnPosMap()));
                segment.getWhereColumns().add(parts);
            }
        }
    }

    @Data
    public static class ValueParts {
        private String identifyColumn;
        private String calculator;
        private Map<String, ValueParts> caseMap;
        private ValueParts caseElseParts;
        private String functionName;
        private List<SqlNode> functionParams;
        private String aliasName;
        private SqlLiteral constantValue;
        private String columnType;
        private SqlNode node;
        private SqlKind sqlKind = SqlKind.IDENTIFIER;
        private String nodeString;
        private Queue<String> polandQueue;

        public ValueParts() {
        }

        public void setNode(SqlNode node) {
            this.node = node;
            this.sqlKind = node.getKind();
        }

        public void setConstantValue(SqlLiteral value) {
            this.constantValue = value;
            this.sqlKind = value.getKind();
        }

        public void setCalculator(SqlNode node) {
            this.nodeString = node.toString();
            this.calculator = node.toString().replace(Quoting.BACK_TICK.string, "").replaceAll("\\s+", "");
            this.sqlKind = SqlKind.LISTAGG;
            if (FilterSqlParser.fourZeOper.matcher(calculator).find()) {
                columnType = Const.META_TYPE_DOUBLE;
                polandQueue = PolandNotationUtil.parsePre(calculator);
            } else {
                columnType = Const.META_TYPE_BOOLEAN;
            }
        }

        public void decideType(SqlLiteral literal) {
            if (columnType == null) {
                if (SqlNumericLiteral.class.isAssignableFrom(literal.getClass())) {
                    SqlNumericLiteral literal1 = (SqlNumericLiteral) literal;
                    if (literal1.getScale() == 0) {
                        if (literal1.getPrec() < 12) {
                            columnType = Const.META_TYPE_INTEGER;
                        } else {
                            columnType = Const.META_TYPE_BIGINT;
                        }
                    } else {
                        columnType = Const.META_TYPE_DOUBLE;
                    }
                } else if (SqlTimeLiteral.class.isAssignableFrom(literal.getClass())) {
                    columnType = Const.META_TYPE_TIMESTAMP;
                } else if (SqlCharStringLiteral.class.isAssignableFrom(literal.getClass())) {
                    columnType = Const.META_TYPE_STRING;
                }
            }
        }
    }


    public static void main(String[] args) {
        String sql = "select a1,a3,(b2+b3)/c1,substr(c4,1,3),case c3 when 1 then 'A' when 2 then 'B' else 'C' end as tag1 from test where ((((a1+a2)+a9)/10>a3 and a4>a5) or a7 in (1,2,3)) or (not ((a3-a6)/a8<b1))";
        String groupSql = "select max(a1),min(a2),sum(c1),c3 from test where ((a1+a2)*a9)/10>a3 group by c3 having sum(c1)>100";

        DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();
        DataCollectionMeta meta = builder.addColumn("a1", Const.META_TYPE_DOUBLE).addColumn("a2", Const.META_TYPE_DOUBLE)
                .addColumn("a3", Const.META_TYPE_DOUBLE).addColumn("a4", Const.META_TYPE_DOUBLE)
                .addColumn("a5", Const.META_TYPE_DOUBLE).addColumn("a6", Const.META_TYPE_DOUBLE)
                .addColumn("a7", Const.META_TYPE_INTEGER).addColumn("a8", Const.META_TYPE_DOUBLE)
                .addColumn("a9", Const.META_TYPE_DOUBLE).addColumn("b1", Const.META_TYPE_DOUBLE)
                .addColumn("b2", Const.META_TYPE_DOUBLE).addColumn("b3", Const.META_TYPE_DOUBLE)
                .addColumn("c1", Const.META_TYPE_DOUBLE).addColumn("c2", Const.META_TYPE_DOUBLE)
                .addColumn("c3", Const.META_TYPE_INTEGER).addColumn("c4", Const.META_TYPE_STRING).tableName("test").build();
        SqlSegment segment = CommSqlParser.parseSingleTableQuerySql(sql, Lex.MYSQL, meta, "N_COLUMN");
        Map<String, Object> map = new HashMap<>();
        map.put("a1", 1.0);
        map.put("a2", 1.0);
        map.put("a3", 11.0);
        map.put("a4", 1.0);
        map.put("a5", 1.0);
        map.put("a6", 1.0);
        map.put("a7", 1);
        map.put("a8", 3.0);
        map.put("a9", 1.0);
        map.put("b1", 1.0);
        map.put("b2", 1.0);
        map.put("b3", 1.0);
        map.put("c1", 1.0);
        map.put("c2", 1.0);
        map.put("c3", 2);
        map.put("c4", "Asdassdasdasd");
        /*if (CommRecordGenerator.recordAcceptable(segment, map)) {
            Map<String, Object> retMap = new HashMap<>();
            CommRecordGenerator.doCalculator(segment, map, retMap);
            log.info("{}",retMap);
        }*/
        SqlSegment segment1= CommSqlParser.parseGroupByAgg(groupSql,Lex.MYSQL,meta,"NCOLUMN");
        System.out.println(segment1);
    }
}
