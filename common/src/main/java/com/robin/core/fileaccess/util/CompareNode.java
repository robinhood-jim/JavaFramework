package com.robin.core.fileaccess.util;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Queue;

@Data
public class CompareNode {
    private Pair<Queue<String>,String> leftNode;
    private Pair<Queue<String>,String> rightNode;
    private String comparator;
    private CompareNode left;
    private CompareNode right;
    private String linkOperator;
    private String notCompareOper;
    public CompareNode(){

    }

    public CompareNode(Pair<Queue<String>,String> leftNode,Pair<Queue<String>,String> rightNode,String comparator,String linkOper,String notCompareOper){
        this.leftNode=leftNode;
        this.rightNode=rightNode;
        this.comparator=comparator;
        this.linkOperator=linkOper;
        this.notCompareOper=notCompareOper;
    }
    public static class Builder{
        private CompareNode node=new CompareNode();
        private Builder(){

        }
        public static Builder newBuilder(){
            return new Builder();
        }
        public Builder leftNode(Pair<Queue<String>,String> leftNode){
            node.leftNode=leftNode;
            return this;
        }
        public Builder rightNode(Pair<Queue<String>,String> rightNode){
            node.rightNode=rightNode;
            return this;
        }
        public Builder comparator(String comparator){
            node.comparator=comparator;
            return this;
        }
        public Builder left(CompareNode left){
            node.left=left;
            return this;
        }
        public Builder right(CompareNode right){
            node.right=right;
            return this;
        }
        public CompareNode build(){
            return node;
        }
    }
    public boolean isEmpty(){
        return leftNode==null && rightNode==null && left==null && right==null;
    }
}
