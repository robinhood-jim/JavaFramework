package com.robin.example.service.system;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;

import com.robin.example.model.user.SysResourceRole;
import com.robin.example.model.user.SysRole;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component(value="sysRoleService")
@Scope(value="singleton")
public class SysRoleService extends BaseAnnotationJdbcService<SysRole, Long> {
	@Transactional(propagation= Propagation.REQUIRED,noRollbackFor=RuntimeException.class)
	public void saveRoleRigth(String[] ids,String resId) throws ServiceException {
		try{
			this.getJdbcDao().deleteByField(SysResourceRole.class, "resId", new Long(resId));
			for (int i = 0; i < ids.length; i++) {
				if(!ids[i].equals("")){
					SysResourceRole resRole=new SysResourceRole();
					resRole.setResId(new Integer(resId));
					resRole.setRoleId(new Integer(ids[i]));
					resRole.setStatus("1");
					this.getJdbcDao().createVO(resRole);
				}
			}
		}catch(DAOException ex){
			ex.printStackTrace();
			throw new ServiceException(ex);
		}
	}
}
