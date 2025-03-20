package com.robin.basis.controller.common;

import com.robin.basis.utils.WebUtils;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.LocalFileSystemAccessor;
import com.robin.core.web.controller.AbstractController;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/ossres")
@Controller
public class OssResourceController extends AbstractController {
    @Resource
    private Environment environment;

    @GetMapping
    @PermitAll
    public void showResource(HttpServletResponse response, @RequestParam String path){
        String resPath=path;
        try {
            AbstractFileSystemAccessor accessor=SpringContextHolder.getBean(AbstractFileSystemAccessor.class);
            if(LocalFileSystemAccessor.class.isAssignableFrom(accessor.getClass()) && environment.containsProperty("oss.startPath")){
                resPath=environment.getProperty("oss.startPath")+path;
            }
            WebUtils.returnOSSResource(response, SpringContextHolder.getBean(AbstractFileSystemAccessor.class), resPath);
        }catch (IOException ex){
            try{
                wrapErrMsg(response,ex.getMessage());
            }catch (IOException ex1){

            }
        }
    }

}
