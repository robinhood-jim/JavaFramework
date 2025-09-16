package com.robin.basis.controller.system;

import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysEmployeeQueryDTO;
import com.robin.basis.mapper.EmployeeMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.Employee;
import com.robin.basis.service.biz.ISmsService;
import com.robin.basis.service.system.IEmployeeService;
import com.robin.core.base.util.Const;
import com.robin.core.template.util.FreeMarkerUtil;
import com.robin.core.web.controller.AbstractMyBatisController;
import com.robin.core.web.spring.anotation.RepeatSubmitCheck;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/system/employee")
public class EmployeeController extends AbstractMyBatisController<IEmployeeService, EmployeeMapper,Employee,Long> {
    private Random random=new Random(10001011L);
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private ISmsService smsService;

    @GetMapping
    public Map<String,Object> list(SysEmployeeQueryDTO dto){
        return wrapObject(service.list(dto));
    }
    @PostMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> saveEmployee(@RequestBody EmployeeDTO dto){
        if(service.saveEmployee(dto,false)){
            return wrapSuccess("");
        }else{
            return wrapFailedMsg("");
        }
    }
    @PutMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> updateEmployee(@RequestBody EmployeeDTO dto){
        if(service.updateEmployee(dto)){
            return wrapSuccess("");
        }else{
            return wrapFailedMsg("");
        }
    }
    @PermitAll
    @GetMapping("/checkExists/{phoneNum}")
    @RepeatSubmitCheck(banIp = true)
    public Map<String,Object> checkExists(@PathVariable String phoneNum){
        long count=service.lambdaQuery().eq(AbstractMybatisModel::getStatus, Const.VALID)
                .eq(Employee::getContactPhone,phoneNum).count();
        if(count>0L){
            return wrapFailedMsg("exists");
        }else{
            return wrapSuccess("OK");
        }
    }
    @PermitAll
    @GetMapping("/verifyCode/{phoneNum}")
    @RepeatSubmitCheck(banIp = true)
    public Map<String,Object> verifyCode(HttpServletRequest request, @PathVariable String phoneNum){
        long count=service.lambdaQuery().eq(AbstractMybatisModel::getStatus, Const.VALID)
                .eq(Employee::getContactPhone,phoneNum).count();
        if(count>0L){
            return wrapFailedMsg("该号码已注册");
        }else{
            int randomNum=random.nextInt(9000)+1000;
            redisTemplate.opsForValue().set("employeeVerify:"+phoneNum,String.valueOf(randomNum), Duration.ofMinutes(2));
            //send verify sms
            if(smsService!=null){
                try(ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(outputStream))) {
                    Map<String, String> map = new HashMap<>();
                    FreeMarkerUtil freeutil = new FreeMarkerUtil(request.getSession().getServletContext(), "/template");
                    map.put("phoneNum",phoneNum);
                    map.put("code",String.valueOf(randomNum));
                    freeutil.process("sms.ftl",map,writer);
                    smsService.sendSms(phoneNum,new String(outputStream.toByteArray()));
                }catch (Exception ex){
                    return wrapError(ex);
                }
            }
            return wrapSuccess("OK");
        }
    }
    @PermitAll
    @PostMapping("/register")
    @RepeatSubmitCheck(banIp = true)
    public Map<String,Object> register(@RequestBody EmployeeDTO dto){
        Assert.notNull(dto.getVerifyCode(),"验证码为空");
        Assert.notNull(dto.getContactPhone(),"手机号为空");
        if(!dto.getVerifyCode().equals(redisTemplate.opsForValue().get("employeeVerify:"+dto.getContactPhone()))){
            return wrapFailedMsg("验证码不正确");
        }
        if(service.saveEmployee(dto,true)){
            return wrapSuccess("注册成功");
        }else{
            return wrapFailedMsg("");
        }


    }
}
