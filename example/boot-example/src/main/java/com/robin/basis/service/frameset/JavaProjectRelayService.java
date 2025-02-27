package com.robin.basis.service.frameset;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.basis.model.frameset.JavaProjectRelay;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component(value="javaProjectRelayService")
@Scope(value="singleton")
public class JavaProjectRelayService extends BaseAnnotationJdbcService<JavaProjectRelay, Long> {
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor = RuntimeException.class)
	public void addProjectRelation(String projId,String[] libraryIds){
		deleteByField("projId", Long.valueOf(projId));
		for (int i = 0; i < libraryIds.length; i++) {
			JavaProjectRelay relay=new JavaProjectRelay();
			relay.setLibraryId(Long.valueOf(libraryIds[i]));
			relay.setProjId(Long.valueOf(projId));
			this.getJdbcDao().createVO(relay,Long.class);
		}	
	}
	
}
