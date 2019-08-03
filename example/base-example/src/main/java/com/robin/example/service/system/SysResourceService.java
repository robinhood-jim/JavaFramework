package com.robin.example.service.system;

import com.robin.core.base.service.BaseAnnotationJdbcService;

import com.robin.example.model.system.SysResource;
import com.robin.example.model.user.SysResourceUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component(value="sysResourceService")
@Scope(value="singleton")
public class SysResourceService extends BaseAnnotationJdbcService<SysResource, Long> {
	@Transactional(propagation= Propagation.REQUIRED,noRollbackFor=RuntimeException.class)
	public void updateUserResourceRight(String userId,List<String> addList,List<String> delList){
		this.getJdbcDao().deleteByField(SysResourceUser.class, "userId", new Integer(userId));
		//添加的权限
		if(addList!=null && !addList.isEmpty()){
			for (String addId:addList) {
				SysResourceUser vo=new SysResourceUser();
				vo.setUserId(new Integer(userId));
				vo.setResId(new Integer(addId));
				vo.setAssignType(SysResourceUser.ASSIGN_ADD);
				vo.setStatus("1");
				this.getJdbcDao().createVO(vo);
			}
		}
		//删除的权限
		if(delList!=null && !delList.isEmpty()){
			for (String delId:delList) {
				SysResourceUser vo=new SysResourceUser();
				vo.setUserId(new Integer(userId));
				vo.setResId(new Integer(delId));
				vo.setAssignType(SysResourceUser.ASSIGN_DEL);
				vo.setStatus("1");
				this.getJdbcDao().createVO(vo);
			}
		}
		
	}
}
