package com.robin.comm.sql;


import cn.hutool.core.util.NumberUtil;
import com.google.common.util.concurrent.*;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.exception.GenericException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.util.Calculator;
import com.robin.core.fileaccess.util.CalculatorCallable;
import com.robin.core.fileaccess.util.CalculatorPool;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Single Table schema record generator
 */
@Slf4j
public class CommRecordGenerator {
    private static CalculatorPool caPool;
    private static ListeningExecutorService pool;
    static {
        caPool=new CalculatorPool();
        pool=MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(20));
    }

    private CommRecordGenerator(){
    }
    /**
     * Adjust input record (Map) fit Selected Condition
     * @param segment   SqlParseSegment
     * @param inputRecord   inputRecord
     * @return
     */
    static boolean recordAcceptable(SqlSegment segment, Map<String, Object> inputRecord) {
        SqlNode whereNode = segment.getWhereCause();
        if (CollectionUtils.isEmpty(segment.getWherePartsMap())) {
            segment.setWherePartsMap(segment.getWhereColumns().stream().collect(Collectors.toMap(CommSqlParser.ValueParts::getNodeString, Function.identity())));
        }
        return walkTree(whereNode, inputRecord, segment);
    }
    public static boolean doesRecordAcceptable(SqlSegment segment, Map<String, Object> inputRecord) {
        SqlNode whereNode = segment.getWhereCause();
        if (CollectionUtils.isEmpty(segment.getWherePartsMap())) {
            segment.setWherePartsMap(segment.getWhereColumns().stream().collect(Collectors.toMap(CommSqlParser.ValueParts::getNodeString, Function.identity())));
        }
        Calculator calculator=null;
        try{
            calculator=caPool.borrowObject();
            calculator.setSegment(segment);
            calculator.setInputRecord(inputRecord);
            return calculator.walkTree(whereNode);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }finally {
            if(calculator!=null) {
                caPool.returnObject(calculator);
            }
        }
        return false;
    }

    /**
     * do Calculate async using ListenableFuture
     * @param segment
     * @param inputRecord
     * @param newRecord
     * @throws Exception
     */
    public static void doAsyncCalculator(SqlSegment segment,Map<String,Object> inputRecord,Map<String,Object> newRecord) throws Exception{
        newRecord.clear();
        List<ListenableFuture<Boolean>> futures=new ArrayList<>();
        try {
            Map<Integer,Throwable> exMap=new HashMap<>();
            for (int i = 0; i < segment.getSelectColumns().size(); i++) {
                Calculator calculator = caPool.borrowObject();
                calculator.clear();
                calculator.setValueParts(segment.getSelectColumns().get(i));
                calculator.setInputRecord(inputRecord);
                calculator.setOutputRecord(newRecord);
                calculator.setSegment(segment);
                ListenableFuture<Boolean> future = pool.submit(new CalculatorCallable(calculator));
                Futures.addCallback(future, new FutureCallback<>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                caPool.returnObject(calculator);
                            }
                            @Override
                            public void onFailure(Throwable throwable) {
                                caPool.returnObject(calculator);
                                exMap.put(1,throwable);
                            }
                        }
                ,pool);
                futures.add(future);
            }
            for(ListenableFuture<Boolean> future:futures){
                future.get();
            }
            if(exMap.get(1)!=null){
                throw new GenericException(exMap.get(1).getMessage());
            }
        }catch (Exception ex){
            log.error("{}",ex);
            throw new GenericException(ex);
        }
    }


    public static void closePool(){
        if(!ObjectUtils.isEmpty(caPool)) {
            caPool.close();
        }
    }

    /**
     * return Selected Column (including calculated column)
     * @param segment
     * @param inputRecord
     * @return
     */
    static void doCalculator(SqlSegment segment,Map<String,Object> inputRecord,Map<String,Object> newRecord){
        newRecord.clear();
        String columnName=null;
        for(int i=0;i<segment.getSelectColumns().size();i++){
            CommSqlParser.ValueParts parts=segment.getSelectColumns().get(i);
            columnName=!ObjectUtils.isEmpty(parts.getAliasName())?parts.getAliasName():parts.getIdentifyColumn();
            if(SqlKind.LISTAGG.equals(parts.getSqlKind())){
                if(!CollectionUtils.isEmpty(parts.getPolandQueue())){
                    Double value=PolandNotationUtil.computeResult(parts.getPolandQueue(),inputRecord);
                    newRecord.put(columnName,value);
                }
            }else if(SqlKind.CASE.equals(parts.getSqlKind())){
                Assert.isTrue(!CollectionUtils.isEmpty(parts.getCaseMap()),"case without switch");
                String cmpValue=null;
                if(!ObjectUtils.isEmpty(inputRecord.get(parts.getIdentifyColumn()))) {
                    cmpValue = inputRecord.get(parts.getIdentifyColumn()).toString();
                }else if(!ObjectUtils.isEmpty(inputRecord.get(parts.getIdentifyColumn().toUpperCase()))){
                    cmpValue = inputRecord.get(parts.getIdentifyColumn().toUpperCase()).toString();
                }
                if(ObjectUtils.isEmpty(cmpValue) && !ObjectUtils.isEmpty(parts.getCaseElseParts())){
                    calculateNode(parts,columnName,inputRecord,newRecord);
                }else{
                    if(parts.getCaseMap().containsKey(cmpValue)){
                        calculateNode(parts.getCaseMap().get(cmpValue),columnName,inputRecord,newRecord);
                    }else if(!ObjectUtils.isEmpty(parts.getCaseElseParts())){
                        calculateNode(parts,columnName,inputRecord,newRecord);
                    }
                }
            }else if(SqlKind.FUNCTION.contains(parts.getSqlKind())){
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
                        Integer nums=((Number)((SqlLiteral)sqlNodes.get(2)).getValue()).intValue();
                        Object value=Optional.ofNullable(inputRecord.get(column)).orElse(inputRecord.get(column.toUpperCase()));
                        if(!ObjectUtils.isEmpty(value)){
                            if(value.toString().length()<=fromPos){
                                newRecord.put(columnName,"");
                            }else {
                                if (value.toString().length() < fromPos + nums) {
                                    nums = value.toString().length() - fromPos;
                                }
                                newRecord.put(columnName, value.toString().substring(fromPos, fromPos+nums));
                            }
                        }
                        break;
                    case "trim":
                        List<SqlNode> childNodes=node.getOperandList();
                        if(SqlLiteral.class.isAssignableFrom(childNodes.get(0).getClass())){
                            newRecord.put(columnName,childNodes.get(0).toString().trim());
                        }else if(SqlIdentifier.class.isAssignableFrom(childNodes.get(0).getClass())){
                            newRecord.put(columnName,inputRecord.get(childNodes.get(0).toString()).toString().trim());
                        }
                        break;
                    case "concat":
                        List<SqlNode> childNodes1=node.getOperandList();
                        for(SqlNode tnode:childNodes1) {
                            if (SqlLiteral.class.isAssignableFrom(tnode.getClass())) {
                                newRecord.put(columnName, childNodes1.get(0).toString().trim());
                            } else if (SqlIdentifier.class.isAssignableFrom(tnode.getClass())) {
                                newRecord.put(columnName, inputRecord.get(tnode.toString()).toString().trim());
                            }
                        }
                    case "decode":

                        break;
                    case "nvl":
                        break;
                    default:


                }
            }
            else if(SqlKind.IDENTIFIER.equals(parts.getSqlKind())){
                if (inputRecord.containsKey(parts.getIdentifyColumn())) {
                    newRecord.put(columnName,inputRecord.get(parts.getIdentifyColumn()));
                }else if(inputRecord.containsKey(parts.getIdentifyColumn().toUpperCase())){
                    newRecord.put(columnName,inputRecord.get(parts.getIdentifyColumn().toUpperCase()));
                }
            }

        }
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
        List<SqlNode> childNodes = ((SqlBasicCall) node).getOperandList();
        if (SqlBasicCall.class.isAssignableFrom(node.getClass()) && (SqlKind.AND.equals(node.getKind()) || SqlKind.OR.equals(node.getKind()))) {
            if (childNodes.size() == 2 && !SqlKind.AND.equals(childNodes.get(1).getKind()) && !SqlKind.OR.equals(childNodes.get(1).getKind())) {
                runValue = walkTree(childNodes.get(1), inputMap, segment);
                if (SqlKind.AND.equals(node.getKind()) && !runValue) {
                    return false;
                }
                if (SqlKind.OR.equals(node.getKind()) && runValue) {
                    return true;
                }
                runValue = walkTree(childNodes.get(0), inputMap, segment);
            } else {
                for (SqlNode node1 : childNodes) {
                    runValue = walkTree(node1, inputMap, segment);
                    if (SqlKind.AND.equals(node.getKind()) && !runValue) {
                        return false;
                    }
                    if (SqlKind.OR.equals(node.getKind()) && runValue) {
                        return true;
                    }
                }
            }
        }else  {
            switch (node.getKind()){
                case GREATER_THAN:
                case LESS_THAN:
                case GREATER_THAN_OR_EQUAL:
                case EQUALS:
                case LESS_THAN_OR_EQUAL:
                case NOT_EQUALS:
                    List<SqlNode> nodes = ((SqlBasicCall) node).getOperandList();
                    WeakReference<Object> leftValue=new WeakReference<>(getValueWithCalculate(inputMap, segment, nodes.get(0)));
                    WeakReference<Object> rightValue = new WeakReference<>(getValueWithCalculate(inputMap,segment,nodes.get(1)));
                    if(!ObjectUtils.isEmpty(leftValue) && !ObjectUtils.isEmpty(rightValue)) {
                        if (NumberUtil.isNumber(leftValue.toString())) {
                            Assert.isTrue(NumberUtil.isNumber(leftValue.toString()) && NumberUtil.isNumber(rightValue.toString()), " only number allowed");
                            runValue= cmpNumber(node.getKind(), (Number) leftValue.get(), (Number) rightValue.get());
                        }else {
                            runValue = doCompare(segment, node, leftValue, rightValue);
                        }
                    }
                    break;
                case IN:
                case NOT_IN:
                    List<SqlNode> sqlNodes = ((SqlBasicCall) node).getOperandList();
                    SqlIdentifier identifier = (SqlIdentifier) sqlNodes.get(0);
                    Set<String> inSets = segment.getInPartMap().get(identifier.toString());
                    if (inputMap.containsKey(identifier.toString())) {
                        runValue= SqlKind.IN.equals(node.getKind()) ? inSets.contains(inputMap.get(identifier.toString())) : !inSets.contains(inputMap.get(identifier.toString()));
                    } else {
                        runValue= false;
                    }
                    break;
                case LIKE:
                    List<SqlNode> sqlNodes1 = ((SqlBasicCall) node).getOperandList();
                    SqlIdentifier identifier1 = (SqlIdentifier) sqlNodes1.get(0);
                    WeakReference<String> likeStr=new WeakReference<>(sqlNodes1.get(1).toString());
                    WeakReference<String> value=new WeakReference<>(inputMap.containsValue(identifier1.toString())?inputMap.get(identifier1.toString()).toString()
                            :inputMap.get(identifier1.toString().toUpperCase()).toString());
                    if(!ObjectUtils.isEmpty(value)) {
                        if (likeStr.get().startsWith("%")) {
                            if (likeStr.get().endsWith("%")) {
                                runValue =value.get().contains(likeStr.get().substring(1,likeStr.get().length()-1));
                            }else{
                                runValue=value.get().endsWith(likeStr.get().substring(1));
                            }
                        } else if (likeStr.get().endsWith("%")) {
                            runValue=value.get().startsWith(likeStr.get().substring(0,likeStr.get().length()-1));
                        }
                    }
                    break;
                case BETWEEN:
                    Assert.isTrue(childNodes.size()==3,"between must have two values");
                    WeakReference<String> cmpColumn=new WeakReference<>(childNodes.get(0).toString());
                    checkColumnNumeric(segment,cmpColumn.get());
                    if(!ObjectUtils.isEmpty(inputMap.get(cmpColumn))) {
                        WeakReference<Number> minVal = new WeakReference<>((Number) ((SqlLiteral) childNodes.get(1)).getValue());
                        WeakReference<Number> manVal = new WeakReference<>((Number) ((SqlLiteral) childNodes.get(2)).getValue());
                        runValue = (Double)inputMap.get(cmpColumn)>=minVal.get().doubleValue() && (Double)inputMap.get(cmpColumn)<=manVal.get().doubleValue();
                    }else{
                        runValue=false;
                    }
                    break;
                case IS_NULL:
                case IS_NOT_NULL:
                    String columnName=((SqlBasicCall)node).getOperandList().get(0).toString();
                    runValue=!ObjectUtils.isEmpty(inputMap.get(columnName)) || !ObjectUtils.isEmpty(inputMap.get(columnName.toUpperCase()));
                    return SqlKind.IS_NOT_NULL.equals(node.getKind())?runValue:!runValue;
                case NOT:
                    runValue = !walkTree(((SqlBasicCall) node).getOperandList().get(0), inputMap, segment);
                    break;
                default:
                    throw new OperationNotSupportException("can not handle this opertator "+node.getKind());
            }
        }
        return runValue;
    }
    protected static void checkColumnNumeric(SqlSegment segment,String columnName){
        Assert.isTrue(Const.META_TYPE_INTEGER.equals(segment.getOriginSchemaMap().get(columnName).getColumnType())
                    || Const.META_TYPE_BIGINT.equals(segment.getOriginSchemaMap().get(columnName).getColumnType())
                    || Const.META_TYPE_DOUBLE.equals(segment.getOriginSchemaMap().get(columnName).getColumnType()),"require numeric column");
    }

    private static Object getValueWithCalculate(Map<String, Object> inputMap, SqlSegment segment, SqlNode nodes) {
        Object value=null;
        if (SqlBasicCall.class.isAssignableFrom(nodes.getClass())) {
            if (segment.getWherePartsMap().containsKey(nodes.toString())) {
                Queue<String> queue = segment.getWherePartsMap().get(nodes.toString()).getPolandQueue();
                value = PolandNotationUtil.computeResult(queue, inputMap);
            } else {
                throw new ConfigurationIncorrectException("");
            }
        } else if (SqlLiteral.class.isAssignableFrom(nodes.getClass())) {
            value = ((SqlLiteral) nodes).getValue();
        } else if (SqlIdentifier.class.isAssignableFrom(nodes.getClass()) && inputMap.containsKey(nodes.toString())) {
            value = inputMap.get(nodes.toString());
        }
        return value;
    }

    private static boolean doCompare(SqlSegment segment,SqlNode node, Object leftValue, Object rightValue) {
        boolean fit = false;
        switch (node.getKind()) {
            case EQUALS:
                if(!ObjectUtils.isEmpty(leftValue) && !ObjectUtils.isEmpty(rightValue)) {
                    fit = leftValue.equals(rightValue);
                }
                break;
            case NOT_EQUALS:
                if(!ObjectUtils.isEmpty(leftValue) && !ObjectUtils.isEmpty(rightValue)) {
                    fit = !leftValue.equals(rightValue);
                }
                break;
            case IN:
            case NOT_IN:
                String columnName=leftValue.toString();
                if(segment.getInPartMap().containsKey(columnName) && segment.getInPartMap().containsKey(rightValue.toString())){
                    fit=SqlKind.IN.equals(node.getKind());
                }
                break;
            case NOT:
                fit=doCompare(segment,((SqlBasicCall)node).getOperandList().get(1),leftValue,rightValue);
                break;
            default:
                throw new OperationNotSupportException("can not handle this opertator "+node.getKind());

        }
        return fit;
    }

    public static boolean cmpNumber( SqlKind comparator,Number left, Number right) {
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
    public static void close(){
        if(pool!=null) {
            pool.shutdown();
        }
        if(caPool!=null){
            caPool.close();
        }
    }

    public static CalculatorPool getCalculatePool() {
        return caPool;
    }
}
