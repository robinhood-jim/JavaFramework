package com.robin.basis.service.system.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysEmployeeQueryDTO;
import com.robin.basis.mapper.EmployeeMapper;
import com.robin.basis.model.region.Region;
import com.robin.basis.model.user.Employee;
import com.robin.basis.service.region.IRegionService;
import com.robin.basis.service.system.IEmployeeService;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl extends AbstractMybatisService<EmployeeMapper, Employee,Long> implements IEmployeeService {
    @Resource
    private IRegionService regionService;

    private static final DateTimeFormatter birthDayFormat=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public Map<String,Object> list(SysEmployeeQueryDTO queryDTO){
        IPage<Employee> page= this.lambdaQuery().like(StrUtil.isNotBlank(queryDTO.getPhone()), Employee::getContactPhone, queryDTO.getPhone())
                .eq(StrUtil.isNotBlank(queryDTO.getDistrict()),Employee::getDistrict,queryDTO.getDistrict())
                .and(StrUtil.isNotBlank(queryDTO.getName()), wrapper -> wrapper.like(Employee::getName, queryDTO.getName())
                        .or(orWrapper -> orWrapper.like(Employee::getAddress, queryDTO.getName()))).page(getPage(queryDTO));
        if(page.getTotal()>0L){
            return WebUtils.toPageVO(page, f->{
                EmployeeDTO dto=EmployeeDTO.fromVO(f);
                if(!ObjectUtil.isEmpty(f.getDistrict())) {
                    List<String> regions = regionService.getRegionLevel(f.getDistrict());
                    if (!CollectionUtils.isEmpty(regions) && regions.size() == 3) {
                        dto.setProvince(regions.get(0));
                        dto.setCity(regions.get(1));
                        dto.setDistrict(regions.get(2));
                    }
                }
                return dto;
            });
        }else{
            return WebUtils.toEmptyPageVO();
        }
    }
    public boolean saveEmployee(EmployeeDTO dto) throws ServiceException{
        int count=this.lambdaQuery().eq(StrUtil.isNotBlank(dto.getCreditNo()),Employee::getCreditNo,dto.getCreditNo())
                .or(orwrapper->orwrapper.eq(StrUtil.isNotBlank(dto.getName()),Employee::getName,dto.getName())).count();
        if(count>0){
            throw new ServiceException("employee exists!");
        }
        Employee employee=new Employee();
        BeanUtils.copyProperties(dto,employee);
        if(!ObjectUtil.isEmpty(dto.getDistrict())) {
            Region region = regionService.getByField(Region::getName, Const.OPERATOR.EQ, dto.getDistrict());
            employee.setDistrict(region.getCode());
        }
        if(!ObjectUtil.isEmpty(dto.getBrithDay())){
            employee.setBrithDay(LocalDate.parse(dto.getBrithDay(),birthDayFormat));
        }
        return save(employee);
    }
    public boolean updateEmployee(EmployeeDTO dto) throws ServiceException{
        Employee employee=getById(dto.getId());
        if(!ObjectUtil.isEmpty(employee)) {
            BeanUtils.copyProperties(dto, employee);
            if(!ObjectUtil.isEmpty(dto.getBrithDay())){
                employee.setBrithDay(LocalDate.parse(dto.getBrithDay(),birthDayFormat));
            }
            if(!ObjectUtil.isEmpty(dto.getDistrict())) {
                Region region = regionService.getByField(Region::getName, Const.OPERATOR.EQ, dto.getDistrict());
                employee.setDistrict(region.getCode());
            }
            return updateById(employee);
        }
        return false;
    }
}
