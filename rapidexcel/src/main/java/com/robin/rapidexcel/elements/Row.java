package com.robin.rapidexcel.elements;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Row {
    private List<Cell> cells;
    public Row(List<Cell> cells){
        this.cells=cells;
    }


}
