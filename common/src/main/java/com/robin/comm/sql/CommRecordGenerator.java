package com.robin.comm.sql;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Sets;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import org.apache.calcite.sql.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommRecordGenerator {
    public static boolean recordAccessible(SqlSegment segment, Map<String, Object> inputRecord) {
        SqlNode whereNode = segment.getWhereCause();
        if (CollectionUtils.isEmpty(segment.getWherePartsMap())) {
            segment.setWherePartsMap(segment.getWhereColumns().stream().collect(Collectors.toMap(CommSqlParser.ValueParts::getNodeString, Function.identity())));
        }
        return walkTree(whereNode, inputRecord, segment);
    }
    public static Map<String,Object> doCalculator(SqlSegment segment,Map<String,Object> inputRecord){
        Map<String,Object> retMap=new HashMap<>();

        String columnName=null;
        for(int i=0;i<segment.getSelectColumns().size();i++){
            CommSqlParser.ValueParts parts=segment.getSelectColumns().get(i);
            columnName=!ObjectUtils.isEmpty(parts.getAliasName())?parts.getAliasName():parts.getIdentifyColumn();
            if(SqlKind.LISTAGG.equals(parts.getSqlKind())){
                if(!CollectionUtils.isEmpty(parts.getPolandQueue())){
                    Double value=PolandNotationUtil.computeResult(parts.getPolandQueue(),inputRecord);
                    retMap.put(columnName,value);
                }
            }else if(SqlKind.CASE.equals(parts.getSqlKind())){
                Assert.isTrue(!CollectionUtils.isEmpty(parts.getCaseMap()));
                String cmpValue=null;
                if(!ObjectUtils.isEmpty(inputRecord.get(parts.getIdentifyColumn()))) {
                    cmpValue = inputRecord.get(parts.getIdentifyColumn()).toString();
                }else if(!ObjectUtils.isEmpty(inputRecord.get(parts.getIdentifyColumn().toUpperCase()))){
                    cmpValue = inputRecord.get(parts.getIdentifyColumn().toUpperCase()).toString();
                }
                if(ObjectUtils.isEmpty(cmpValue) && !ObjectUtils.isEmpty(parts.getCaseElseParts())){
                    calculateNode(parts,columnName,inputRecord,retMap);
                }else{
                    if(parts.getCaseMap().containsKey(cmpValue)){
                        calculateNode(parts.getCaseMap().get(cmpValue),columnName,inputRecord,retMap);
                    }else if(!ObjectUtils.isEmpty(parts.getCaseElseParts())){
                        calculateNode(parts,columnName,inputRecord,retMap);
                    }
                }
            }else if(SqlKind.FUNCTION.equals(parts.getSqlKind()) ||SqlKind.OTHER_FUNCTION.equals(parts.getSqlKind())){
                SqlBasicCall node=(SqlBasicCall) parts.getNode();
                SqlOperator operator= node.getOperator();
                SqlIdentifier identifier= operator.getNameAsId();
                String operName=identifier.getSimple().toLowerCase();
                switch (operName){
                    case "substr":
                        List<SqlNode> sqlNodes=node.getOperandList();
                        Assert.isTrue(sqlNodes.size()==3,"substr function must have three parameters");
                        Assert.isTrue(SqlKind.IDENTIFIER.equals(sqlNodes.get(0).getKind())," first parameter must be column");
                        String column=sqlNodes.get(0).toString();
                        if(!Const.META_TYPE_STRING.equals(segment.getOriginSchemaMap().get(column).getColumnType())){
                            throw new OperationNotSupportException("only string column can substr");
                        }
                        Integer fromPos=((Number)((SqlLiteral)sqlNodes.get(1)).getValue()).intValue();
                        Integer toPos=((Number)((SqlLiteral)sqlNodes.get(1)).getValue()).intValue();
                        Object value=Optional.ofNullable(inputRecord.get(column)).orElse(inputRecord.get(column.toUpperCase()));
                        if(!ObjectUtils.isEmpty(value)){
                            if(value.toString().length()<=fromPos){
                                retMap.put(columnName,"");
                            }else {
                                if (value.toString().length() < fromPos + toPos) {
                                    toPos = value.toString().length() - fromPos;
                                }
                                retMap.put(columnName, value.toString().substring(fromPos, toPos));
                            }
                        }
                        break;
                    case "decode":
                        break;
                    case "nvl":
                        break;

                }
            }
            else if(SqlKind.IDENTIFIER.equals(parts.getSqlKind())){
                if (inputRecord.containsKey(parts.getIdentifyColumn())) {
                    retMap.put(columnName,inputRecord.get(parts.getIdentifyColumn()));
                }else if(inputRecord.containsKey(parts.getIdentifyColumn().toUpperCase())){
                    retMap.put(columnName,inputRecord.get(parts.getIdentifyColumn().toUpperCase()));
                }

            }

        }
        return retMap;
    }
    private static void calculateNode(CommSqlParser.ValueParts parts,String columnName,Map<String,Object> inputRecord,Map<String,Object> retMap){
        if(!ObjectUtils.isEmpty(parts.getConstantValue())){
            if(!SqlCharStringLiteral.class.isAssignableFrom(parts.getConstantValue().getClass())){
                retMap.put(columnName,parts.getConstantValue().getValue());
            }else{
                retMap.put(columnName,parts.getConstantValue().toString().replace("'",""));
            }
        }
        if(SqlKind.LISTAGG.equals(parts.getSqlKind())){
            if(!CollectionUtils.isEmpty(parts.getPolandQueue())){
                Double value=PolandNotationUtil.computeResult(parts.getPolandQueue(),inputRecord);
                retMap.put(columnName,value);
            }
        }else if(SqlKind.IDENTIFIER.equals(parts.getSqlKind())){
            if(inputRecord.containsKey(parts.getIdentifyColumn())){
                retMap.put(columnName,inputRecord.get(parts.getIdentifyColumn()));
            }else if(inputRecord.containsKey(parts.getIdentifyColumn().toUpperCase())){
                retMap.put(columnName,inputRecord.get(parts.getIdentifyColumn().toUpperCase()));
            }else{
                retMap.put(columnName,null);
            }
        }
    }


    private static boolean walkTree(SqlNode node, Map<String, Object> inputMap, SqlSegment segment) {
        boolean runValue = false;
        boolean processTag = false;
        if (SqlBasicCall.class.isAssignableFrom(node.getClass()) && SqlKind.AND.equals(node.getKind()) || SqlKind.OR.equals(node.getKind())) {
            List<SqlNode> sqlNodes = ((SqlBasicCall) node).getOperandList();
            if (sqlNodes.size() == 2 && !SqlKind.AND.equals(sqlNodes.get(1).getKind()) && !SqlKind.OR.equals(sqlNodes.get(1).getKind())) {
                runValue = walkTree(sqlNodes.get(1), inputMap, segment);
                if (SqlKind.AND.equals(node.getKind()) && !runValue) {
                    return false;
                }
                if (SqlKind.OR.equals(node.getKind()) && runValue) {
                    return true;
                }
                runValue = walkTree(sqlNodes.get(0), inputMap, segment);
            } else {
                for (SqlNode node1 : sqlNodes) {
                    runValue = walkTree(node1, inputMap, segment);
                    if (SqlKind.AND.equals(node.getKind()) && !runValue) {
                        return false;
                    }
                    if (SqlKind.OR.equals(node.getKind()) && runValue) {
                        return true;
                    }
                }
            }
            processTag = true;
        }
        if (!processTag) {
            if (SqlKind.GREATER_THAN.equals(node.getKind()) || SqlKind.LESS_THAN.equals(node.getKind()) || SqlKind.GREATER_THAN_OR_EQUAL.equals(node.getKind())
                    || SqlKind.EQUALS.equals(node.getKind()) || SqlKind.LESS_THAN_OR_EQUAL.equals(node.getKind()) || SqlKind.NOT_EQUALS.equals(node.getKind())) {
                SqlNode[] nodes = ((SqlBasicCall) node).getOperandList().toArray(new SqlNode[0]);
                Object lefValue = null;
                Object rightValue = null;
                if (SqlBasicCall.class.isAssignableFrom(nodes[0].getClass())) {
                    if (segment.getWherePartsMap().containsKey(nodes[0].toString())) {
                        Queue<String> queue = segment.getWherePartsMap().get(nodes[0].toString()).getPolandQueue();
                        lefValue = PolandNotationUtil.computeResult(queue, inputMap);
                    } else {
                        throw new ConfigurationIncorrectException("");
                    }
                } else if (SqlLiteral.class.isAssignableFrom(nodes[0].getClass())) {
                    lefValue = ((SqlLiteral) nodes[0]).getValue();
                } else if (SqlIdentifier.class.isAssignableFrom(nodes[0].getClass())) {
                    if (inputMap.containsKey(nodes[0].toString())) {
                        lefValue = inputMap.get(nodes[0].toString());
                    }
                }
                if (SqlBasicCall.class.isAssignableFrom(nodes[1].getClass())) {
                    if (segment.getWherePartsMap().containsKey(nodes[1].toString())) {
                        Queue<String> queue = segment.getWherePartsMap().get(nodes[1].toString()).getPolandQueue();
                        rightValue = PolandNotationUtil.computeResult(queue, inputMap);
                    } else {
                        throw new ConfigurationIncorrectException("");
                    }
                } else if (SqlIdentifier.class.isAssignableFrom(nodes[1].getClass())) {
                    if (inputMap.containsKey(nodes[1].toString())) {
                        rightValue = inputMap.get(nodes[1].toString());
                    }
                } else if (SqlLiteral.class.isAssignableFrom(nodes[1].getClass())) {
                    rightValue = ((SqlLiteral) nodes[1]).getValue();
                }
                if (NumberUtil.isNumber(lefValue.toString())) {
                    Assert.isTrue(NumberUtil.isNumber(lefValue.toString()) && NumberUtil.isNumber(rightValue.toString()), " only number allowed");
                    return cmpNumber((Number) lefValue, (Number) rightValue, node.getKind());
                }
                return doCompare(node.getKind(), lefValue, rightValue);
            } else if (SqlKind.IN.equals(node.getKind()) || SqlKind.NOT_IN.equals(node.getKind())) {
                List<SqlNode> sqlNodes = ((SqlBasicCall) node).getOperandList();
                SqlIdentifier identifier = (SqlIdentifier) sqlNodes.get(0);
                Set<String> inSets = segment.getInPartMap().computeIfAbsent(identifier.toString(), k -> {
                    Set<String> sets = Sets.newHashSet();
                    for (int i = 1; i < sqlNodes.size(); i++) {
                        sets.add(sqlNodes.get(i).toString());
                    }
                    return sets;
                });
                if (inputMap.containsKey(identifier.toString())) {
                    return SqlKind.IN.equals(node.getKind()) ? inSets.contains(inputMap.get(identifier.toString())) : !inSets.contains(inputMap.get(identifier.toString()));
                } else {
                    return false;
                }
            } else if (SqlKind.NOT.equals(node.getKind())) {
                runValue = !walkTree(((SqlBasicCall) node).getOperandList().get(0), inputMap, segment);
            }
        }
        return runValue;
    }

    private static boolean doCompare(SqlKind kind, Object leftValue, Object rightValue) {
        boolean fit = false;
        switch (kind) {


        }
        return fit;
    }

    public static boolean cmpNumber(Number left, Number right, SqlKind comparator) {
        boolean retValue = false;
        if (Double.class.isAssignableFrom(left.getClass()) || Double.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.doubleValue() > right.doubleValue();
                    break;
                case LESS_THAN:
                    retValue = left.doubleValue() < right.doubleValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.doubleValue() >= right.doubleValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.doubleValue() <= right.doubleValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.doubleValue() != right.doubleValue();
                    break;
                case EQUALS:
                    retValue = left.doubleValue() == right.doubleValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }

        } else if (Integer.class.isAssignableFrom(left.getClass()) || Integer.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.intValue() > right.intValue();
                    break;
                case LESS_THAN:
                    retValue = left.intValue() < right.intValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.intValue() >= right.intValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.intValue() <= right.intValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.intValue() != right.intValue();
                    break;
                case EQUALS:
                    retValue = left.intValue() == right.intValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        } else if (Long.class.isAssignableFrom(left.getClass()) || Long.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.longValue() > right.longValue();
                    break;
                case LESS_THAN:
                    retValue = left.longValue() < right.longValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.longValue() >= right.longValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.longValue() <= right.longValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.longValue() != right.longValue();
                    break;
                case EQUALS:
                    retValue = left.longValue() == right.longValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        }
        return retValue;
    }
}
