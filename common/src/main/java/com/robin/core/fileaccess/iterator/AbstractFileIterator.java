/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.fileaccess.iterator;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Sets;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.ApacheVfsFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.CompareNode;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;


public abstract class AbstractFileIterator implements IResourceIterator {
    protected BufferedReader reader;
    protected InputStream instream;
    protected AbstractFileSystemAccessor accessUtil;
    protected String identifier;
    protected DataCollectionMeta colmeta;
    protected List<String> columnList = new ArrayList<>();
    protected Map<String, DataSetColumnMeta> columnMap = new HashMap<>();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Map<String, Object> filterMap = new HashMap<>();
    protected String filterSql = "";
    protected static final Pattern fourZeOper = Pattern.compile("\\+|-|\\*|/");
    protected static final Pattern pattern = Pattern.compile("<>|>|<|=|>=|<=|in|not in|exists|not|between");
    protected boolean useFilter = false;
    protected Map<String, Object> cachedValue = new HashMap<>();
    protected final Set<String> numberPermitOperators = Sets.newHashSet(">", "<", ">=", "<=", "<>", "between", "not");
    // filterSql parse compare tree
    protected CompareNode rootNode = null;
    protected Map<String, Set<String>> inPartMap = new HashMap<>();
    protected Map<String, Pair<Double, Double>> rangeMap = new HashMap<>();
    // if filterSql has four operations,orc and parquet can not use filter directly
    protected boolean hasFourOperations = false;
    // if filterSql compare right hand is column,orc and parquet can not use filter directly
    protected boolean hasRightColumnCmp = false;

    public AbstractFileIterator() {

    }

    public AbstractFileIterator(DataCollectionMeta colmeta) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && !ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(ResourceConst.STORAGEFILTERSQL))){
            withFilterSql(colmeta.getResourceCfgMap().get(ResourceConst.STORAGEFILTERSQL).toString());
        }

    }

    public AbstractFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessUtil) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
        this.accessUtil = accessUtil;
    }

    @Override
    public void beforeProcess() {
        checkAccessUtil(colmeta.getPath());
        Assert.notNull(accessUtil, "ResourceAccessUtil is required!");
        try {
            Pair<BufferedReader, InputStream> pair = accessUtil.getInResourceByReader(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
            this.reader = pair.getKey();
            this.instream = pair.getValue();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void afterProcess() {
        try {
            close();
        } catch (IOException ex) {
            logger.error("{}", ex.getMessage());
        }
    }


    protected void checkAccessUtil(String inputPath) {
        try {
            if (accessUtil == null) {
                URI uri = new URI(StringUtils.isEmpty(inputPath) ? colmeta.getPath() : inputPath);
                String schema = !ObjectUtils.isEmpty(colmeta.getFsType()) ? colmeta.getFsType() : uri.getScheme();
                accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void setInputStream(InputStream stream) {
        this.instream = stream;
    }

    protected void copyToLocal(File tmpFile, InputStream stream) {
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            IOUtils.copyBytes(stream, outputStream, 8192);
        } catch (IOException ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (instream != null) {
            instream.close();
        }
        if (accessUtil != null) {
            if (ApacheVfsFileSystemAccessor.class.isAssignableFrom(accessUtil.getClass())) {
                if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(Const.ITERATOR_PROCESSID))) {
                    ((ApacheVfsFileSystemAccessor) accessUtil).closeWithProcessId(colmeta.getResourceCfgMap().get(Const.ITERATOR_PROCESSID).toString());
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public AbstractFileSystemAccessor getFileSystemAccessor() {
        return accessUtil;
    }

    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        this.accessUtil = accessUtil;
    }

    @Override
    public boolean hasNext() {
        try {
            pullNext();
            while (useFilter && !walkTree(rootNode, cachedValue)) {
                pullNext();
            }
            return !CollectionUtils.isEmpty(cachedValue);
        } catch (Exception ex) {
            throw new MissingConfigException(ex);
        }
    }

    @Override
    public Map<String, Object> next() {
        return cachedValue;
    }

    public void withFilterSql(String filterSql) {
        this.filterSql = filterSql;
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
            parseNotation(operator, compareOper, tMap, rootNodeMap);
            pos += splits[i].length();
            splitPart = filterSql.substring(pos + operator.length());
        }
        rootNode = rootNodeMap.get(1);
        useFilter = true;
    }

    private void parseNotation(String comparator, String compareOper, Map<Integer, Integer> tMap, Map<Integer, CompareNode> rootNodeMap) {
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
            hasFourOperations = true;
            leftPair = Pair.of(queue, null);
        } else {
            leftPair = Pair.of(null, doFill(leftOper, tMap).replace("(", "").replace(")", "").trim());
        }

        if ("in".equalsIgnoreCase(linkOper) || "not in".equalsIgnoreCase(linkOper)) {
            rightOper = rightOper.replace("(", "").replace(")", "").trim();
            Set<String> sets = Sets.newHashSet(rightOper.split(","));
            inPartMap.put(leftPair.getValue(), sets);
        } else if ("between".equalsIgnoreCase(linkOper)) {
            rightOper = rightOper.replace("(", "").replace(")", "").trim();
            String[] obj = rightOper.split(",", -1);
            rangeMap.put(leftPair.getValue(), Pair.of(Optional.of(obj[0]).map(Double::valueOf).orElse(Double.MIN_VALUE), Optional.of(obj[1]).map(Double::valueOf).orElse(Double.MAX_VALUE)));
        }
        if (fourZeOper.matcher(rightOper).find()) {
            Queue queue = PolandNotationUtil.parsePre(doFill(rightOper, tMap));
            rightPair = Pair.of(queue, null);
        } else {
            rightPair = Pair.of(null, doFill(rightOper, tMap).replace("(", "").replace(")", "").trim());
        }
        if (rightPair.getValue() != null && columnMap.containsKey(rightPair.getValue())) {
            hasRightColumnCmp = true;
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

    private String doFill(String input, Map<Integer, Integer> depthMap) {
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

    protected abstract void pullNext();


    protected boolean walkTree(CompareNode node, Map<String, Object> valueMap) {
        boolean calculatorVal = false;
        if (node.getRightNode() != null && node.getLeftNode() != null) {
            calculatorVal = doCompare(node, valueMap);
        } else {
            if (node.getRight().getLeftNode() != null) {
                calculatorVal = walkTree(node.getRight(), valueMap);
                if ("or".equalsIgnoreCase(node.getRight().getComparator()) && calculatorVal) {
                    return true;
                } else if ("and".equalsIgnoreCase(node.getRight().getComparator()) && !calculatorVal) {
                    return false;
                }
                calculatorVal = walkTree(node.getLeft(), valueMap);
            } else {
                if (node.getLeft() != null) {
                    calculatorVal = walkTree(node.getLeft(), valueMap);
                } else {
                    if (node.getRight() != null) {
                        if ("or".equalsIgnoreCase(node.getRight().getComparator()) && calculatorVal) {
                            return true;
                        } else if ("and".equalsIgnoreCase(node.getRight().getComparator()) && !calculatorVal) {
                            return false;
                        }
                        calculatorVal = walkTree(node.getRight(), valueMap);
                    }
                }
            }

        }
        return calculatorVal;
    }

    private boolean doCompare(CompareNode node, Map<String, Object> valueMap) {
        Object leftValue = doCalculate(node.getLeftNode(), valueMap);
        Object rightValue = doCalculate(node.getRightNode(), valueMap);
        if (rightValue == null) {
            rightValue = leftValue;
            leftValue = node.getLeftNode().getValue();
        }
        boolean calculatorVal = false;
        if (numberPermitOperators.contains(node.getLinkOperator()) && NumberUtil.isNumber(leftValue.toString())) {
            calculatorVal = compareNumber(node, leftValue, rightValue, node.getLinkOperator());
        } else {
            calculatorVal = cmpObject(node, leftValue, rightValue, node.getLinkOperator());
        }
        return calculatorVal;
    }

    private Object doCalculate(Pair<Queue<String>, String> pair, Map<String, Object> map) {
        Object retObj = Optional.ofNullable(pair.getLeft()).map(f -> PolandNotationUtil.computeResult(f, map)).orElse(null);
        if (retObj == null && !ObjectUtils.isEmpty(pair.getRight())) {
            retObj = Optional.ofNullable(map.get(pair.getRight())).orElse(map.get(pair.getRight().toUpperCase()));
        }
        return retObj;
    }

    protected boolean compareNumber(CompareNode node, Object leftValue, Object rightValue, String comparator) {
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
                if (rangeMap.containsKey(leftValue.toString())) {
                    if (Double.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).doubleValue() > rangeMap.get(leftValue.toString()).getKey() && ((Number) rightValue).doubleValue() < rangeMap.get(leftValue.toString()).getValue();
                    } else if (Integer.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).intValue() > rangeMap.get(leftValue.toString()).getKey().intValue() && ((Number) rightValue).intValue() < rangeMap.get(leftValue.toString()).getValue().intValue();
                    } else if (Long.class.isAssignableFrom(rightValue.getClass())) {
                        retVal = ((Number) rightValue).longValue() > rangeMap.get(leftValue.toString()).getKey().longValue() && ((Number) rightValue).longValue() < rangeMap.get(leftValue.toString()).getValue().longValue();
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

    protected boolean cmpObject(CompareNode node, Object leftValue, Object rightValue, String comparator) {
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
                retVal = !cmpObject(node, leftValue, rightValue, node.getNotCompareOper());
                break;
            default:
                throw new MissingConfigException("can not handle");
        }
        return retVal;
    }

    private boolean cmpNumber(Number left, Number right, String comparator) {
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
                default:
                    retValue = left.doubleValue() == right.doubleValue();
                    break;
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
                default:
                    retValue = left.intValue() == right.intValue();
                    break;
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
                default:
                    retValue = left.longValue() == right.longValue();
                    break;
            }
        }
        return retValue;
    }
}
