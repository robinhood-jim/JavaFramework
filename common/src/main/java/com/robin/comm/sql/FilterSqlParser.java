package com.robin.comm.sql;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Sets;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterSqlParser {
    protected static final Pattern fourZeOper = Pattern.compile("\\+|-|\\*|/");
    protected static final Pattern pattern = Pattern.compile("<>|>|<|=|>=|<=|in|not in|exists|not|between|like");
    protected static final Set<String> numberPermitOperators = Sets.newHashSet(">", "<", ">=", "<=", "<>", "between", "not");
    private FilterSqlParser(){

    }
    public static boolean isFourOperation(String sqlPart){
        return fourZeOper.matcher(sqlPart).find();
    }
    public static FilterSqlResult doParse(DataCollectionMeta colmeta,String filterSql){
        Assert.isTrue(!ObjectUtils.isEmpty(colmeta) && !CollectionUtils.isEmpty(colmeta.getColumnList()),"column meta defined required!");
        FilterSqlResult result=new FilterSqlResult(colmeta);

        String[] splits = filterSql.split("and|or|AND|OR");
        int pos = 0;
        String splitPart = null;
        List<Pair<String, String>> operList = new ArrayList<>();
        Map<Integer, Integer> tMap = new HashMap<>();
        tMap.put(1, 0);
        Map<Integer, CompareNode> rootNodeMap = new HashMap<>();
        rootNodeMap.put(1, null);

        for (int i = 0; i < splits.length; i++) {
            String compareOper = splits[i];

            String operator = "";
            if (!StringUtils.isEmpty(splitPart)) {
                String[] remains = splitPart.split(" ");
                operator = remains[0].trim().equalsIgnoreCase("or") || remains[0].trim().equalsIgnoreCase("and") ? remains[0] : remains[1];
            }
            operList.add(Pair.of(operator, compareOper));
            parseNotation(result,operator, compareOper, tMap, rootNodeMap);
            pos += splits[i].length();
            splitPart = filterSql.substring(pos + operator.length());
        }
        result.setRootNode(rootNodeMap.get(1));
        return result;
    }

    private static void parseNotation(FilterSqlResult result,String comparator, String compareOper, Map<Integer, Integer> tMap, Map<Integer, CompareNode> rootNodeMap) {
        String[] mathOpers = compareOper.split(pattern.pattern());
        String leftOper = mathOpers[0];
        String rightOper = mathOpers[1];
        String left = compareOper.substring(leftOper.length());
        int operPos = left.indexOf(rightOper);
        String linkOper = left.substring(0, operPos);
        String notLinkOper = null;
        if ("not".equalsIgnoreCase(linkOper)) {
            int pos = compareOper.toLowerCase().indexOf("not");
            String remain = compareOper.substring(pos + 3);
            mathOpers = remain.split(pattern.pattern());
            leftOper = mathOpers[0];
            rightOper = mathOpers[1];
            left = remain.substring(leftOper.length());
            operPos = left.indexOf(rightOper);
            notLinkOper = left.substring(0, operPos);
        }
        Pair<Queue<String>, String> leftPair;
        Pair<Queue<String>, String> rightPair;

        if (fourZeOper.matcher(leftOper).find()) {
            Queue<String> queue = PolandNotationUtil.parsePre(doFill(leftOper, tMap));
            result.setHasFourOperations(true);
            leftPair = Pair.of(queue, null);
        } else {
            leftPair = Pair.of(null, doFill(leftOper, tMap).replace("(", "").replace(")", "").trim());
        }

        if ("in".equalsIgnoreCase(linkOper) || "not in".equalsIgnoreCase(linkOper)) {
            rightOper = rightOper.replace("(", "").replace(")", "").trim();
            Set<String> sets = Sets.newHashSet(rightOper.split(","));
            result.getInPartMap().put(leftPair.getValue(), sets);
        } else if ("between".equalsIgnoreCase(linkOper)) {
            rightOper = rightOper.replace("(", "").replace(")", "").trim();
            String[] obj = rightOper.split(",", -1);
            result.getRangeMap().put(leftPair.getValue(), Pair.of(Optional.of(obj[0]).map(Double::valueOf).orElse(Double.MIN_VALUE), Optional.of(obj[1]).map(Double::valueOf).orElse(Double.MAX_VALUE)));
        }
        rightOper=rightOper.replace("'","");
        if (fourZeOper.matcher(rightOper).find()) {
            Queue queue = PolandNotationUtil.parsePre(doFill(rightOper, tMap));
            rightPair = Pair.of(queue, null);
        } else {
            rightPair = Pair.of(null, doFill(rightOper, tMap).replace("(", "").replace(")", "").trim());
        }
        if (rightPair.getValue() != null && result.getColumnMap().containsKey(rightPair.getValue())) {
            result.setHasRightColumnCmp(true);
        }
        if (rootNodeMap.get(1) == null) {
            CompareNode rootNode = new CompareNode(leftPair, rightPair, comparator, linkOper, notLinkOper);
            rootNodeMap.put(1, rootNode);
        } else {
            CompareNode.Builder builder = CompareNode.Builder.newBuilder();
            CompareNode rootNode = builder.left(rootNodeMap.get(1)).right(new CompareNode(leftPair, rightPair, comparator, linkOper, notLinkOper)).build();
            rootNodeMap.put(1, rootNode);
        }
    }

    private static String doFill(String input, Map<Integer, Integer> depthMap) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '(') {
                depthMap.put(1, depthMap.get(1) + 1);
            }
            if (input.charAt(i) == ')') {
                if (depthMap.get(1) > 0) {
                    depthMap.put(1, depthMap.get(1) - 1);
                    builder.append(input.charAt(i));
                }
            } else {
                builder.append(input.charAt(i));
            }
        }
        if (depthMap.get(1) > 0) {
            for (int j = 0; j < depthMap.get(1); j++) {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    public static boolean walkTree(FilterSqlResult result,CompareNode node, Map<String, Object> valueMap) {
        boolean calculatorVal = false;
        if (node.getRightNode() != null && node.getLeftNode() != null) {
            calculatorVal = doCompare(result,node, valueMap);
        } else {
            if (node.getRight().getLeftNode() != null) {
                calculatorVal = walkTree(result,node.getRight(), valueMap);
                if ("or".equalsIgnoreCase(node.getRight().getComparator()) && calculatorVal) {
                    return true;
                } else if ("and".equalsIgnoreCase(node.getRight().getComparator()) && !calculatorVal) {
                    return false;
                }
                calculatorVal = walkTree(result,node.getLeft(), valueMap);
            } else {
                if (node.getLeft() != null) {
                    calculatorVal = walkTree(result,node.getLeft(), valueMap);
                } else {
                    if (node.getRight() != null) {
                        if ("or".equalsIgnoreCase(node.getRight().getComparator()) && calculatorVal) {
                            return true;
                        } else if ("and".equalsIgnoreCase(node.getRight().getComparator()) && !calculatorVal) {
                            return false;
                        }
                        calculatorVal = walkTree(result,node.getRight(), valueMap);
                    }
                }
            }

        }
        return calculatorVal;
    }

    private static boolean doCompare(FilterSqlResult result,CompareNode node, Map<String, Object> valueMap) {
        Object leftValue = doCalculate(node.getLeftNode(), valueMap);
        Object rightValue = doCalculate(node.getRightNode(), valueMap);
        if (rightValue == null ){
            if(("in".equalsIgnoreCase(node.getLinkOperator()) || "not in".equalsIgnoreCase(node.getLinkOperator()) || "like".equalsIgnoreCase(node.getLinkOperator()))) {
                rightValue = leftValue;
                leftValue = node.getLeftNode().getValue();
            }else{
                if(NumberUtil.isNumber(node.getRightNode().getValue())) {
                    rightValue = Double.parseDouble(node.getRightNode().getValue());
                }
            }
        }
        if(leftValue==null){
            leftValue=node.getLeftNode().getValue();
        }

        boolean calculatorVal = false;

        if (numberPermitOperators.contains(node.getLinkOperator()) && NumberUtil.isNumber(leftValue.toString())) {
            calculatorVal = compareNumber(result,node, leftValue, rightValue, node.getLinkOperator());
        } else {
            calculatorVal = cmpObject(result.getInPartMap(),result.getColumnMap(),node, leftValue, rightValue, node.getLinkOperator());
        }
        return calculatorVal;
    }

    private static Object doCalculate(Pair<Queue<String>, String> pair, Map<String, Object> map) {
        Object retObj = Optional.ofNullable(pair.getLeft()).map(f -> PolandNotationUtil.computeResult(f, map)).orElse(null);
        if (retObj == null && !ObjectUtils.isEmpty(pair.getRight())) {
            retObj = Optional.ofNullable(map.get(pair.getRight())).orElse(map.get(pair.getRight().toUpperCase()));
            if(retObj==null){
                if(NumberUtil.isNumber(pair.getRight())) {
                    retObj=Double.parseDouble(pair.getRight());
                }else{
                    retObj = pair.getRight();
                }
            }
        }

        return retObj;
    }

    protected static boolean compareNumber(FilterSqlResult result,CompareNode node, Object leftValue, Object rightValue, String comparator) {
        if (numberPermitOperators.contains(comparator)) {
            Assert.isTrue(Number.class.isAssignableFrom(rightValue.getClass()), "only number permited!");
        }
        boolean retVal = false;
        switch (comparator.toLowerCase()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "=":
            case "<>":
                retVal = cmpNumber((Number) leftValue, (Number) rightValue, comparator);
                break;
            case "between":
                if (result.getRangeMap().containsKey(leftValue.toString())) {
                    if (Double.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).doubleValue() > result.getRangeMap().get(leftValue.toString()).getKey() && ((Number) rightValue).doubleValue() < result.getRangeMap().get(leftValue.toString()).getValue();
                    } else if (Integer.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).intValue() > result.getRangeMap().get(leftValue.toString()).getKey().intValue() && ((Number) rightValue).intValue() < result.getRangeMap().get(leftValue.toString()).getValue().intValue();
                    } else if (Long.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).longValue() > result.getRangeMap().get(leftValue.toString()).getKey().longValue() && ((Number) rightValue).longValue() < result.getRangeMap().get(leftValue.toString()).getValue().longValue();
                    }
                }
                break;
            case "not":
                retVal = !cmpNumber((Number) leftValue, (Number) rightValue, node.getNotCompareOper());
                break;
            default:
                throw new MissingConfigException("can not handle");
        }
        return retVal;
    }

    protected static boolean cmpObject(Map<String, Set<String>> inPartMap,Map<String,DataSetColumnMeta> columnMap,CompareNode node, Object leftValue, Object rightValue, String comparator) {
        boolean retVal = false;
        switch (comparator) {
            case "=":
                retVal = leftValue.equals(rightValue);
                break;
            case "<>":
                retVal = !leftValue.equals(rightValue);
                break;
            case "in":
            case "not in":
                if (inPartMap.containsKey(leftValue.toString())) {
                    retVal = inPartMap.get(leftValue.toString()).contains(rightValue.toString());
                    if ("not in".equalsIgnoreCase(comparator)) {
                        retVal = !retVal;
                    }
                } else {
                    retVal = false;
                }
                break;
            case "not":
                retVal = !cmpObject(inPartMap,columnMap,node, leftValue, rightValue, node.getNotCompareOper());
                break;
            case "like":
                Assert.isTrue(Const.META_TYPE_STRING.equals(columnMap.get(node.getLeftNode().getValue()).getColumnType()),"only string column can use like");
                String fitStr=node.getRightNode().getValue();
                if(fitStr.startsWith("%")){
                    if(fitStr.endsWith("%")){
                        retVal=leftValue.toString().contains(fitStr.substring(1,fitStr.length()-1));
                    }else {
                        retVal = leftValue.toString().endsWith(fitStr.substring(1));
                    }
                }else if(fitStr.endsWith("%")){
                    retVal = leftValue.toString().startsWith(fitStr.substring(0,fitStr.length()-1));
                }else{
                    retVal=leftValue.toString().contains(fitStr);
                }
                break;
            default:
                throw new MissingConfigException("can not handle");
        }
        return retVal;
    }

    public static boolean cmpNumber(Number left, Number right, String comparator) {
        boolean retValue = false;
        if (Double.class.isAssignableFrom(left.getClass()) || Double.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case ">":
                    retValue = left.doubleValue() > right.doubleValue();
                    break;
                case "<":
                    retValue = left.doubleValue() < right.doubleValue();
                    break;
                case ">=":
                    retValue = left.doubleValue() >= right.doubleValue();
                    break;
                case "<=":
                    retValue = left.doubleValue() <= right.doubleValue();
                    break;
                case "<>":
                    retValue = left.doubleValue() != right.doubleValue();
                    break;
                case "=":
                    retValue = left.doubleValue() == right.doubleValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }

        } else if (Integer.class.isAssignableFrom(left.getClass())) {
            switch (comparator) {
                case ">":
                    retValue = left.intValue() > right.intValue();
                    break;
                case "<":
                    retValue = left.intValue() < right.intValue();
                    break;
                case ">=":
                    retValue = left.intValue() >= right.intValue();
                    break;
                case "<=":
                    retValue = left.intValue() <= right.intValue();
                    break;
                case "<>":
                    retValue = left.intValue() != right.intValue();
                    break;
                case "=":
                    retValue = left.intValue() == right.intValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        } else if (Long.class.isAssignableFrom(left.getClass())) {
            switch (comparator) {
                case ">":
                    retValue = left.longValue() > right.longValue();
                    break;
                case "<":
                    retValue = left.longValue() < right.longValue();
                    break;
                case ">=":
                    retValue = left.longValue() >= right.longValue();
                    break;
                case "<=":
                    retValue = left.longValue() <= right.longValue();
                    break;
                case "<>":
                    retValue = left.longValue() != right.longValue();
                    break;
                case "=":
                    retValue = left.longValue() == right.longValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        }
        return retValue;
    }
    @Data
    public static class FilterSqlResult {

        private Map<String, DataSetColumnMeta> columnMap = new HashMap<>();
        private Map<String, Set<String>> inPartMap = new HashMap<>();
        private Map<String, Pair<Double, Double>> rangeMap = new HashMap<>();
        // if filterSql has four operations,orc and parquet can not use filter directly
        private boolean hasFourOperations = false;
        // if filterSql compare right hand is column,orc and parquet can not use filter directly
        private boolean hasRightColumnCmp = false;
        private CompareNode rootNode;
        private FilterSqlResult(){

        }
        private FilterSqlResult(DataCollectionMeta colmeta){
            columnMap=colmeta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnName, Function.identity()));
        }
        public static class Builder{
            private FilterSqlResult result=new FilterSqlResult();
            public static Builder newBuilder(){
                return new Builder();
            }
            public Builder withMetaConfig(DataCollectionMeta colmeta){
                result.columnMap=colmeta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnName, Function.identity()));
                return this;
            }
            public FilterSqlResult build(){
                return result;
            }
        }
    }
    public static void main(String[] args){

        //iterator.withFilterSql("((((a1+a2)+a9)/10>a3 and a4>a5) or a7 in (1,2,3)) or (not ((a3-a6)/a8<b1))");
        Map<String,Object> valueMap=new HashMap<>();
        valueMap.put("a1",10);
        valueMap.put("a2",20);
        valueMap.put("a9",5);
        valueMap.put("a3",2.4);
        valueMap.put("a4",3.6);
        valueMap.put("a5",3.1);
        valueMap.put("a7",4);
        valueMap.put("a6",77);
        valueMap.put("a8",10);
        valueMap.put("b1",1.0);
        //System.out.println(iterator.walkTree(iterator.rootNode,valueMap));

    }
}
