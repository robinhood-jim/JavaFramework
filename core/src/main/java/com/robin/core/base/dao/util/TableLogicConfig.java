package com.robin.core.base.dao.util;

import lombok.Data;

@Data
public class TableLogicConfig {
    private String columnName;
    private String validValue;
    private String invalidValue;
}
