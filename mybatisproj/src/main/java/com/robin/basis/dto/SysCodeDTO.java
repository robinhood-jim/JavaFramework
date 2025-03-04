package com.robin.basis.dto;

import com.robin.basis.model.system.SysCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysCodeDTO implements Serializable {
    private String value;
    private String label;
    public static SysCodeDTO fromVO(SysCode code){
        SysCodeDTO dto=new SysCodeDTO();
        dto.setValue(code.getItemValue());
        dto.setLabel(code.getItemName());
        return dto;
    }
}
