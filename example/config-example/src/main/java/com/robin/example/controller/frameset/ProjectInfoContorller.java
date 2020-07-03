package com.robin.example.controller.frameset;

import com.robin.comm.subversion.util.GitUtil;
import com.robin.comm.subversion.util.SvnUtil;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.compress.util.CompressUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.template.util.FreeMarkerUtil;
import com.robin.core.web.codeset.Code;
import com.robin.core.web.controller.BaseCrudDhtmlxController;
import com.robin.core.web.codeset.CodeSetService;
import com.robin.example.model.frameset.DataSource;
import com.robin.example.model.frameset.DbDriver;
import com.robin.example.model.frameset.ProjectInfo;
import com.robin.example.service.frameset.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/system/project")

public class ProjectInfoContorller extends BaseCrudDhtmlxController<ProjectInfo, Long, ProjectInfoService> {


    @Autowired
    private DbDriverService dbDriverService;
    @Autowired
    private EntityMappingService entityMappingService;
    @Autowired
    private JavaProjectRelayService javaProjectRelayService;
    @Autowired
    private JdbcDao jdbcDao;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private CodeSetService codeSetUtil;


    private String projId;

    @RequestMapping("/show")
    public String showProject(HttpServletRequest request, HttpServletResponse response) {
        return "project/project_list";
    }

    @RequestMapping("/save")
    @ResponseBody
    public Map<String, Object> saveProjectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            ProjectInfo projectInfo = new ProjectInfo();
            ConvertUtil.mapToObject(projectInfo, wrapRequest(request));
            //DataSourceService DataSourceService = (DataSourceService) getBean("DataSourceService");
            this.service.saveEntity(projectInfo);
            wrapSuccess(retmap);
        } catch (ServiceException e) {
            wrapFailed(retmap,e);
        } catch (Exception e) {
            wrapFailed(retmap,e);
        }
        return retmap;
    }

    @RequestMapping("/presisttype")
    @ResponseBody
    public Map<String, Object> getPresistType(HttpServletRequest request, HttpServletResponse response) {
        List<Code> list = getCodeList(codeSetUtil.getCacheCode("PRESISTTYPE"));
        return wrapComoboWithCode(list, false);
    }

    @RequestMapping("/webframe")
    @ResponseBody
    public Map<String, Object> getWebFrame(HttpServletRequest request, HttpServletResponse response) {
        List<Code> list = getCodeList(codeSetUtil.getCacheCode("WEBFRAME"));
        return wrapComoboWithCode(list, false);
    }

    @RequestMapping("/projecttype")
    @ResponseBody
    public Map<String, Object> getProjectType(HttpServletRequest request, HttpServletResponse response) {
        List<Code> list = getCodeList(codeSetUtil.getCacheCode("PROJECTTYPE"));
        return wrapComoboWithCode(list, false);
    }

    @RequestMapping("/teamtype")
    @ResponseBody
    public Map<String, Object> getTeamType(HttpServletRequest request, HttpServletResponse response) {
        List<Code> list = getCodeList(codeSetUtil.getCacheCode("TEAMTYPE"));
        return wrapComoboWithCode(list, false);
    }

    @RequestMapping("/datasource")
    @ResponseBody
    public Map<String, Object> getDataSources(HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> list4 = dbDriverService.queryBySql("select id,name from t_base_datasource");
        return wrapComobo(list4, "id", "name", false);
    }

    @RequestMapping("/gencvs/{id}")
    @ResponseBody
    public Map<String, Object> genCvs(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws Exception {
        try {
            ProjectInfo projectInfo = this.service.getEntity(id);
            if ("1".equals(projectInfo.getTeamType())) {

            } else if ("2".equals(projectInfo.getTeamType())) {
                GitUtil.cloneProject("test", projectInfo.getTeamUrl(), projectInfo.getProjBasePath(), "robinjim", "robin7704");
            }
            return wrapSuccess("OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            return wrapError(ex);
        }
    }

    @RequestMapping("/view/{id}")
    @ResponseBody
    public Map<String, Object> viewProjectInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'viewProjectIn fo' method");
        }
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            //DataSourceService DataSourceService = (DataSourceService) getBean("DataSourceService");
            ProjectInfo projectInfo = this.service.getEntity(id);
            ConvertUtil.objectToMapObj(retmap, projectInfo);
            wrapSuccess(retmap, "OK");
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }

    @RequestMapping("/edit/{id}")
    @ResponseBody
    public Map<String, Object> editProjectInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'editProjectInfo' method");
        }
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            ProjectInfo projectInfo = this.service.getEntity(id);
            retmap.put("success", true);
            retmap.put("project", projectInfo);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }


    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateProjectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'updateProjectInfo' method");
        }
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            ProjectInfo projectInfo = new ProjectInfo();
            ConvertUtil.mapToObject(projectInfo, wrapRequest(request));
            this.service.updateEntity(projectInfo);
            retmap.put("success", true);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteProjectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'deleteProjectInfo' method");
        }
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            String del_ids = request.getParameter("ids");
            if (del_ids != null && del_ids.trim().length() > 0) {
                this.service.deleteEntity(parseId(del_ids));
            }
            retmap.put("success", true);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }

    public String queryDriverType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String dbtype = request.getParameter("dbType");
            List<DbDriver> list = dbDriverService.queryByField("dbType", BaseObject.OPER_EQ, Long.valueOf(dbtype));
            //JaksonUtil.wrapHttpRequestList(list, response);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    @RequestMapping("/javalib")
    @ResponseBody
    public Map<String, Object> showLibaray(HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> libList = jdbcDao.queryBySql("select id,library_name as name,version from t_base_javalibrary");
        return wrapComobo(libList, "id", "name", false);
    }


    public String listLibrary(HttpServletRequest request, HttpServletResponse response) {
        projId = request.getParameter("id");
        try {
            ProjectInfo info = this.service.getEntity(Long.valueOf(projId));

            List<Map<String, Object>> libList = jdbcDao.queryBySql("select id,library_name as name,version from t_base_javalibrary");
            List<Map<String, Object>> selectlibList = jdbcDao.queryBySql("select a.id as id from t_base_javalibrary a,t_base_projrelay b where a.id=b.library_id and b.proj_id=?", new Object[]{info.getId()});
            Map<String, String> selectmap = new HashMap<String, String>();
            for (int i = 0; i < selectlibList.size(); i++) {
                selectmap.put(selectlibList.get(i).get("id").toString(), "1");
            }
            for (int i = 0; i < libList.size(); i++) {
                if (selectmap.containsKey(libList.get(i).get("id"))) {
                    libList.get(i).put("check", "true");
                } else {
                    libList.get(i).put("check", "false");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "SHOWLIB";
    }

    public String saveLibrary(HttpServletRequest request, HttpServletResponse response) {
        String projId = request.getParameter("projId");
        String[] ids = request.getParameter("ids").split(";");

        javaProjectRelayService.addProjectRelation(projId, ids);
        request.setAttribute("msg", "");
        return null;
    }

    @RequestMapping("/genconfig/{id}")
    @ResponseBody
    public Map<String, Object> genConfig(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) {

        try {
            ProjectInfo info = this.service.getEntity(id);
            String projType = info.getProjType().toString();
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            List<Map<String, Object>> driverList = jdbcDao.queryBySql("select db_type as dbType,driver_class as driverClass from t_base_dbdriver where id=?", new Object[]{source.getDriverId()});
            String basePath = info.getProjBasePath();

            Map<String, Object> parammap = new HashMap<String, Object>();
            parammap.put("project", info);
            parammap.put("source", source);
            parammap.put("driver", driverList.get(0));
            String propertsrc = basePath + "src/main/resources/";
            FreeMarkerUtil freeutil = new FreeMarkerUtil(request.getSession().getServletContext(), "/template");

            FileUtils.forceMkdir(new File(propertsrc));
            String applicationpropfile = propertsrc + "application.properties";
            generateCode(freeutil, applicationpropfile, "applicationresource.ftl", parammap);
            String springcfgsrc = basePath + "src/main/resources/";
            FileUtils.forceMkdir(new File(springcfgsrc));
            String springresourcefile = springcfgsrc + "applicationContext-resource.xml";
            generateCode(freeutil, springresourcefile, "springresource.ftl", parammap);
            //web.xml
            String webxmlfile = basePath + "src/main/webapp/web.xml";
            FileUtils.forceMkdir(new File(basePath + "src/main/webapp/"));
            generateCode(freeutil, webxmlfile, "webxml.ftl", parammap);

            String libbasePath = "web/WEB-INF/lib/";
            parammap.put("libBasePath", libbasePath);
            if ("1".equals(projType)) {
                parammap.put("classOutPut", "bin");
            } else if ("2".equals(projType)) {
                parammap.put("classOutPut", "web/WEB-INF/classes");
            } else {
                parammap.put("classOutPut", "bin");
            }
            List<Map<String, Object>> libraryList = jdbcDao.queryBySql("select a.zip_file as file from t_base_javalibrary a,t_base_projrelay b where a.id=b.library_id and b.proj_id=?", new Object[]{info.getId()});
            List<Map<String, Object>> jarlist = jdbcDao.queryBySql("select d.file_name as jarPath,d.version as version,a.id as libid,d.maven_group as `group`,d.maven_artifact as artifact from t_base_javalibrary a,t_base_projrelay b,t_base_javalibrary_r c,t_base_jar d where a.id=b.library_id  and c.library_id=a.id and c.jar_id=d.id and b.proj_id=? order by a.id", new Object[]{info.getId()});
            parammap.put("relayjarList", jarlist);
            if ("1".equals(info.getJarmanType())) {
                generateCode(freeutil, basePath + "pom.xml", "mavenconfig.ftl", parammap);
            }
            List<Map<String, Object>> templist = jdbcDao.queryBySql("select name,template_path as path from t_base_codetemplate");
            Map<String, List<Map<String, Object>>> tempmap = CollectionBaseConvert.convertToMapByParentKeyWithObjVal(templist, "name");
            Map<String, String> projectConfig = this.service.getTemplateNameByProject(info);
            String webfrm = projectConfig.get("webFramePrefix");

            String webbasepath = request.getSession().getServletContext().getRealPath(tempmap.get(webfrm + "initpack").get(0).get("path").toString());
            CompressUtils.unzip(new File(webbasepath), new File(basePath + "src/main/webapp/"));

            FileUtils.forceMkdir(new File(basePath + "/.settings"));
            for (int i = 0; i < libraryList.size(); i++) {
                String path = request.getSession().getServletContext().getRealPath("/etc/" + libraryList.get(i).get("file"));
                //CompressUtils.unzip(new File(path), new File(zipsrc));
            }

            generateCode(freeutil, basePath + "/.classpath", "classpathdesc.ftl", parammap);
            generateCode(freeutil, basePath + "/.project", "projectdesc.ftl", parammap);
            generateCode(freeutil, basePath + "/.settings/org.eclipse.core.resources.prefs", "resourcepref.ftl", parammap);
            generateCode(freeutil, basePath + "/.settings/org.eclipse.jdt.core.prefs", "jdtpref.ftl", parammap);
            return wrapSuccess("OK");
        } catch (Exception ex) {
            return wrapError(ex);
        }
    }

    @RequestMapping("/checkin/{id}")
    @ResponseBody
    public Map<String, Object> checkInCode(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws Exception {
        try {
            ProjectInfo info = this.service.getEntity(id);
            String teamType = info.getTeamType();
            String teamUrl = info.getTeamUrl();
            String comment = "Commited by Frame Wizard";
            if (request.getParameter("comment") != null && !request.getParameter("comment").isEmpty()) {
                comment = request.getParameter("comment");
            }
            if ("1".equals(teamType)) {
                String localPath = info.getProjBasePath();
                SvnUtil util = new SvnUtil(teamUrl + "/" + info.getProjCode());
                util.authSvn("luoming", "123");
                util.makeDirectory(util.getReposUrl(), comment);
                util.importDirectory(new File(localPath), comment, true);
            } else if ("2".equals(teamType)) {
                GitUtil.addResource(info.getProjBasePath(), ".");
                GitUtil.commit(info.getProjBasePath(), comment, "robinjim", "robin7704");
                GitUtil.pull(info.getProjBasePath());
            }
            return wrapSuccess("OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            return wrapError(ex);
        }
    }

    public void generateCode(FreeMarkerUtil util, String filepath, String templateName, Map parammap) throws Exception {
        PrintWriter writer = new PrintWriter(new File(filepath));
        util.process(templateName, parammap, writer);
        writer.close();
    }

    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> searchProjectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'searchProjectInfo' method");
        }
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            //DataSourceService DataSourceService = (DataSourceService) getBean("DataSourceService");
            PageQuery pageQuery = wrapPageQuery(request);
            pageQuery.setSelectParamId("GET_PROJECTINFO");
            pageQuery.setPageSize(1);
            String queryString = getQueryString(pageQuery);
            pageQuery.getParameters().put("queryString", queryString);
            if (pageQuery.getOrder() == null || "".equals(pageQuery.getOrder().trim())) {
                pageQuery.setOrder("id");
                pageQuery.setOrderDirection("desc");
            }
            this.service.queryBySelectId(pageQuery);
            setCode("PRESISTTYPE,WEBFRAME,YNTYPE");
            filterListByCodeSet(pageQuery, "presist", "PRESISTTYPE",null);
            filterListByCodeSet(pageQuery, "webFrame", "WEBFRAME",null);
            retmap = wrapDhtmlxGridOutput(pageQuery);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }


    private String getQueryString(PageQuery pageQuery) {
        StringBuffer buffer = new StringBuffer();
        /** Add Query Code here **/
        String orgname = pageQuery.getParameters().get("name");
        if (orgname != null && orgname.length() > 0) {
            buffer.append(" and a.proj_name like '%" + orgname + "%'");
        }
        String webfrmId = pageQuery.getParameters().get("webfrmId");
        if (webfrmId != null && webfrmId.length() > 0) {
            buffer.append(" and  a.webframe_id= '" + webfrmId + "'");
        }
        return buffer.toString();
    }

    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        return null;
    }

    public String getProjId() {
        return projId;
    }


    public void setProjId(String projId) {
        this.projId = projId;
    }


}
