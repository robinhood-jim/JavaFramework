package com.robin.rapidexcel.elements;

import lombok.Data;

@Data
public class RelationShip {
    private String id;
    private String target;
    private String type;
    public RelationShip(String id,String target,String type){
        this.id=id;
        this.target=target;
        this.type=type;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }
}
