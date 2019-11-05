package com.robin.example.service.frameset;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.example.model.frameset.JavaLibrary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value="javaLibraryService")
@Scope(value="singleton")
public class JavaLibraryService extends BaseAnnotationJdbcService<JavaLibrary, Long> {

	

}
