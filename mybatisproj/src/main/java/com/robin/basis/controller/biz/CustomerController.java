package com.robin.basis.controller.biz;

import com.robin.basis.dto.CustomerDTO;
import com.robin.basis.dto.query.CustomerQueryDTO;
import com.robin.basis.mapper.biz.CustomerMapper;
import com.robin.basis.service.biz.ICustomerService;
import com.robin.basis.utils.WebUtils;
import com.robin.biz.model.Customer;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
public class CustomerController extends AbstractMyBatisController<ICustomerService, CustomerMapper, Customer,Long> {
    @GetMapping
    public Map<String,Object> list(CustomerQueryDTO dto){
        return WebUtils.toPageVO(queryPage(dto, CustomerDTO.class),null);
    }
    @PostMapping
    public Map<String,Object> save(@RequestBody Map<String,Object> reqMap){
        return doSave(reqMap,null);
    }
    @PutMapping
    public Map<String,Object> update(@RequestBody Map<String,Object> reqMap){
        return doUpdate(reqMap,Long.valueOf(reqMap.get("id").toString()));
    }
    @DeleteMapping
    public Map<String,Object> delete(@RequestBody List<Long> ids ){
        return doDeleteLogic(ids);
    }
}
