package com.robin.core.fileaccess.util;

import cn.hutool.core.util.NumberUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class PolandNotationUtil {
    private static Map<String, Integer> operationSymbolMap = new HashMap<>();
    private static Map<String,Integer> boolOperSymbolMap=new HashMap<>();
    private static String leftChar = "(";
    private static String rightChar = ")";
    private static ThreadLocal<Double> leftVal=new ThreadLocal<>();
    private static ThreadLocal<Double> rightVal=new ThreadLocal<>();
    private static ThreadLocal<Queue<String>> calcuQueue=new ThreadLocal<>();
    static{
        operationSymbolMap.put(")",00); //右括号需匹配左括号，故优先级最低
        operationSymbolMap.put("+",10);
        operationSymbolMap.put("-",10);
        operationSymbolMap.put("*",20);
        operationSymbolMap.put("/",20);
        operationSymbolMap.put("(",30);
        boolOperSymbolMap.put(")",00);
        boolOperSymbolMap.put("&",10);
    }
    public static Queue<String> parsePre(String formula){
        Stack<String> preStack = new Stack<>();
        Queue<String> queue = new LinkedBlockingQueue();
        StringBuilder builder=new StringBuilder();
        char[] chars=formula.toCharArray();
        int i = 0;
        while(i<chars.length && Objects.nonNull(chars[i])) {
            if(isOperator(chars[i])){
                if(builder.length()>0){
                    queue.add(builder.toString().trim());
                    builder.delete(0,builder.length());
                }
                if(preStack.isEmpty()){
                    preStack.push(String.valueOf(chars[i]));
                }else{
                    String top = preStack.pop();
                    if(comparePriority(String.valueOf(chars[i]), top) < 0) {
                        if(top.equals(leftChar)) {
                            preStack.push(top);
                            preStack.push(String.valueOf(chars[i]));
                        }else if(String.valueOf(chars[i]).equals(rightChar)) {
                            appendTo(queue, top);
                            preStack.pop();
                        } else{
                            appendTo(queue, top);
                            popPre(preStack, String.valueOf(chars[i]), queue);
                            preStack.push(String.valueOf(chars[i])); //当前元素入栈
                        }
                    } else {
                        preStack.push(top);
                        preStack.push(String.valueOf(chars[i]));
                    }
                }
            }else{
                builder.append(chars[i]);
            }
            i++;
        }
        if(builder.length()>0){
            queue.add(builder.toString().trim());
        }
        while (!preStack.isEmpty()) {
            queue.add(preStack.pop().trim());
        }

        return queue;
    }
    private static void popPre(Stack<String> preStatck, String charTemp, Queue queue) {
        if(!preStatck.isEmpty()) {
            String top = preStatck.pop();
            if(comparePriority(charTemp, top) <= 0) {
                //低于栈顶元素，成为后缀表达式一部分
                appendTo(queue, top);
                popPre(preStatck, charTemp, queue);
            } else {
                preStatck.push(top);
            }
        }
    }

    private static void appendTo(Queue queue, String s) {
        if(!s.equals(leftChar) && !s.equals(rightChar)) {
            queue.add(s.trim());
        }
    }

    /**
     * 比较优先级
     * @param start
     * @param to
     * @return
     */
    private static int comparePriority(String start, String to) {
        return operationSymbolMap.get(start).compareTo(operationSymbolMap.get(to));
    }


    /**
     * 计算后缀表达式结果
     * @param queue
     * @return
     */
    public static Double computeResult(Queue<String> queue,Map<String,Object> valueMap) {
        double result = 0.0;
        if(CollectionUtils.isEmpty(queue)) {
            return null;
        }
        if(calcuQueue.get()==null) {
            calcuQueue.set(new LinkedBlockingQueue());
        }
        calcuQueue.get().addAll(queue);
        String s = calcuQueue.get().poll();
        Stack<Double> stack = new Stack();

        try {
            while (Objects.nonNull(s)) {
                if (!isOperator(s)) {
                    if (valueMap.get(s) != null) {
                        stack.push(Double.valueOf(valueMap.get(s).toString()));
                    } else {
                        if (NumberUtil.isNumber(s)) {
                            stack.push(Double.valueOf(s));
                        } else {
                            stack.push(0.0);
                        }
                    }
                } else if (!StringUtils.isEmpty(s)) {
                    switch (s) {
                        case "+":
                            leftVal.set(stack.pop());
                            rightVal.set(stack.pop());
                            result = leftVal.get() + rightVal.get();
                            stack.push(result);
                            break;
                        case "-":
                            leftVal.set(stack.pop());
                            rightVal.set(stack.pop());
                            result = rightVal.get() - leftVal.get();
                            stack.push(result);
                            break;
                        case "*":
                            leftVal.set(stack.pop());
                            rightVal.set(stack.pop());
                            result = leftVal.get() * rightVal.get();
                            stack.push(result);
                            break;
                        case "/":
                            leftVal.set(stack.pop());
                            rightVal.set(stack.pop());
                            Assert.isTrue(leftVal.get() != 0.0, "divided value is zero");
                            result = rightVal.get() / leftVal.get();
                            stack.push(result);
                            break;
                    }
                }
                s = calcuQueue.get().poll();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }
    public static void freeMem(){
        if(leftVal!=null) {
            leftVal.remove();
        }
        if(rightVal!=null) {
            rightVal.remove();
        }
        if(calcuQueue!=null) {
            calcuQueue.remove();
        }
    }

    /**
     * 根据逆波兰计算得分
     * @param expression
     * @param valueMap
     * @return
     */
    public static Double computeResult(String expression,Map<String, Object> valueMap){
        Queue<String> queue=parsePre(expression);
        return computeResult(queue,valueMap);
    }
    private static boolean isOperator(char input){
        return operationSymbolMap.containsKey(String.valueOf(input));
    }
    private static boolean isOperator(String input){
        return operationSymbolMap.containsKey(input);
    }
    public static void main(String[] args){
        String exp="(ind1+ind2)/(ind3-ind4)+ind5";
        Map<String,Pair<Double,Double>> valueMap=new HashMap<>();
        valueMap.put("ind1",Pair.of(111.0,0.1));
        valueMap.put("ind2",Pair.of(1450.1,0.7));
        valueMap.put("ind3",Pair.of(1290.1,0.8));
        valueMap.put("ind4",Pair.of(1114.0,0.1));
        valueMap.put("ind5",Pair.of(1234.9,0.3));
        Queue<String> queue=parsePre(exp);
        System.out.println(queue);
        //System.out.println(computeResult(queue,valueMap));
    }


}
