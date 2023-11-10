package com.robin.basis.controller.frameset;

import com.robin.basis.model.frameset.DataSource;
import com.robin.basis.model.frameset.EntityMapping;
import com.robin.basis.model.frameset.FieldMapping;
import com.robin.basis.model.frameset.ProjectInfo;
import com.robin.basis.service.frameset.DataSourceService;
import com.robin.basis.service.frameset.EntityMappingService;
import com.robin.basis.service.frameset.FieldMappingService;
import com.robin.basis.service.frameset.ProjectInfoService;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.datameta.*;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.compress.util.CompressUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.Condition;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditions;
import com.robin.core.template.util.FreeMarkerUtil;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.core.web.util.DhtmxTreeWrapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping("/system/datamapping")
public class DataMappingContoller extends AbstractCrudDhtmlxController<ProjectInfo, Long, ProjectInfoService> {
    @Autowired
    private ProjectInfoService projectInfoService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private EntityMappingService entityMappingService;
    @Autowired
    private FieldMappingService fieldMappingService;
    @Autowired
    private JdbcDao jdbcDao;


    private List<DataBaseColumnMeta> fields;
    private EntityMapping mapping;
    private static final String COL_PROJID = "projId";

    @RequestMapping("/showschema")
    public String showSchema(HttpServletRequest request, HttpServletResponse response) {
        return "/datamapping/mapping_tree";
    }

    @RequestMapping("/showfields")
    public String showFields(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String schema = request.getParameter("schema");
        String projId = request.getParameter(COL_PROJID);
        String table = request.getParameter("table");
        ProjectInfo info = projectInfoService.getEntity(Long.valueOf(projId));
        DataSource source = dataSourceService.getEntity(info.getDataSourceId());
        FilterConditions conditions = new FilterConditions();
        conditions.withCondition(new FilterCondition("proj_id", Condition.EQUALS, info.getId())).withCondition(new FilterCondition("source_id", Condition.EQUALS, source.getId()))
                .withCondition(new FilterCondition("db_schema", Condition.EQUALS, schema)).withCondition(new FilterCondition("entity_code", Condition.EQUALS, table));
        request.setAttribute("sourceId", source.getId());
        List<EntityMapping> mappinglist = entityMappingService.queryByCondition(conditions, new PageQuery());
        if (mappinglist != null && !mappinglist.isEmpty()) {
            mapping = mappinglist.get(0);
            request.setAttribute("mappingId", mapping.getId());
        }
        setCode("PKGEN,PKTYPE,DATATYPE,YNTYPE,FIELDMAP,FIELDDISPLAY");
        request.setAttribute("keyTypeList", findCodeSetArr("PKGEN"));
        request.setAttribute("pkTypeList", findCodeSetArr("PKTYPE"));
        request.setAttribute("ynList", findCodeSetArr("YNTYPE"));
        request.setAttribute("typeList", findCodeSetArr("FIELDMAP"));
        request.setAttribute("displayList", findCodeSetArr("FIELDDISPLAY"));
        return "/datamapping/mapping_fields";
    }

    @RequestMapping("/listschema")
    @ResponseBody
    public String listSchema(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter(COL_PROJID);
        DataBaseUtil util = null;
        String ret = "";
        try {
            ProjectInfo info = projectInfoService.getEntity(Long.valueOf(id));
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            DataBaseParam param = new DataBaseParam(source.getHostIp(), Integer.parseInt(source.getPort()), source.getDatabaseName(), source.getUserName(), source.getPassword());
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(type, param);
            util = new DataBaseUtil();
            util.connect(meta);
            List<String> list = util.getAllShcema();
            ret = DhtmxTreeWrapper.WrappSingleTreeXml(list);
        } catch (Exception ex) {
            if (util != null) {
                util.closeConnection();
            }
        }
        return ret;
    }

    @RequestMapping("/listtable")
    @ResponseBody
    public String listTable(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter(COL_PROJID);
        String schema = request.getParameter("id");
        DataBaseUtil util = null;
        String ret = "";
        try {
            ProjectInfo info = projectInfoService.getEntity(Long.valueOf(id));
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            DataBaseParam param = new DataBaseParam(source.getHostIp(), Integer.parseInt(source.getPort()), source.getDatabaseName(), source.getUserName(), source.getPassword());
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(type, param);
            util = new DataBaseUtil();
            util.connect(meta);
            List<DataBaseTableMeta> list = util.getAllTable(schema);
            ret = DhtmxTreeWrapper.WrappObjectTreeRetXml(list, schema, "tableName", "remark", null, false);
        } catch (Exception ex) {
            if (util != null) {
                util.closeConnection();
            }
        }
        return ret;
    }

    @RequestMapping("/listfields")
    @ResponseBody
    public Map<String, Object> listField(HttpServletRequest request, HttpServletResponse response) {
        String schema = request.getParameter("schema");
        String projId = request.getParameter(COL_PROJID);
        String table = request.getParameter("table");
        DataBaseUtil util = null;
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, Object> retmap;
        try {
            ProjectInfo info = projectInfoService.getEntity(Long.valueOf(projId));
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            DataBaseParam param = new DataBaseParam(source.getHostIp(), Integer.parseInt(source.getPort()), source.getDatabaseName(), source.getUserName(), source.getPassword());
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(type, param);
            util = new DataBaseUtil();
            util.connect(meta);
            List<DataBaseColumnMeta> collist = util.getTableMetaByTableName(table, schema);
            for (DataBaseColumnMeta meta1 : collist) {
                Map<String, String> map = new HashMap<>();
                String columnName = meta1.getColumnName();
                map.put("columnName", meta1.getColumnName());
                map.put("columnType", meta1.getColumnType().toString());
                columnName = columnName.replace("-", "").replaceAll("_", "").replaceAll("$", "");
                map.put("propName", columnName);
                map.put("propType", "");
                map.put("isNull", meta1.isNullable() ? "1" : "0");
                if (meta1.getComment() != null) {
                    map.put("name", meta1.getComment());
                }
                map.put("displayType", "1");
                if (!meta1.isPrimaryKey()) {
                    map.put("showIngrid", "true");
                    map.put("showInquery", "true");
                    map.put("editable", "true");
                }
                list.add(map);
            }
            FilterConditions filterConditions = new FilterConditions();
            filterConditions.withCondition(new FilterCondition("proj_id", Condition.EQUALS, info.getId())).withCondition(new FilterCondition("source_id", Condition.EQUALS, source.getId()))
                    .withCondition(new FilterCondition("db_schema", Condition.EQUALS, schema)).withCondition(new FilterCondition("entity_code", Condition.EQUALS, table));

            List<EntityMapping> mappinglist = entityMappingService.queryByCondition(filterConditions, new PageQuery());
            if (mappinglist != null && !mappinglist.isEmpty()) {
                mapping = mappinglist.get(0);

                List<FieldMapping> fieldmapList = fieldMappingService.queryByField("entityId", BaseObject.OPER_EQ, mapping.getId());
                for (int i = 0; i < fieldmapList.size(); i++) {
                    list.get(i).put("propName", fieldmapList.get(i).getCode());
                    list.get(i).put("propType", fieldmapList.get(i).getType());
                    list.get(i).put("displayType", fieldmapList.get(i).getDisplayType());
                    list.get(i).put("showIngrid", fieldmapList.get(i).getShowIngrid());
                    list.get(i).put("showInquery", fieldmapList.get(i).getShowInquery());
                    list.get(i).put("editable", fieldmapList.get(i).getEditable());
                    if ("1".equals(fieldmapList.get(i).getIsPrimary())) {

                    }
                }
            }
            setCode("PKGEN,PKTYPE,DATATYPE,YNTYPE,FIELDMAP,FIELDDISPLAY");
            filterListByCodeSet(list, "columnType", "DATATYPE");
            retmap = wrapDhtmlxGridOutputWithNoCheck(list, "columnName,columnType,propName,propType,name,displayType,showIngrid,showInquery,editable", "columnName");

        } catch (Exception ex) {
            request.setAttribute("err", ex);
            return Collections.emptyMap();
        } finally {
            if (util != null) {
                util.closeConnection();
            }
        }
        return retmap;
    }

    @RequestMapping("/saveMapping")
    @ResponseBody
    public Map<String, Object> saveMapping(HttpServletRequest request, HttpServletResponse response) {
        String schema = request.getParameter("schema");
        String projId = request.getParameter(COL_PROJID);
        String table = request.getParameter("table");
        DataBaseUtil util = null;
        Map<String, Object> retMap = new HashMap<>();
        try {
            ProjectInfo info = projectInfoService.getEntity(Long.valueOf(projId));
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            DataBaseParam param = new DataBaseParam(source.getHostIp(), Integer.parseInt(source.getPort()), source.getDatabaseName(), source.getUserName(), source.getPassword());
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(type, param);
            util = new DataBaseUtil();
            util.connect(meta);
            List<DataBaseColumnMeta> collist = util.getTableMetaByTableName(table, schema);
            String mappingId = request.getParameter("mappingId");
            if (mappingId != null && !mappingId.isEmpty()) {
                mapping = entityMappingService.getEntity(Long.valueOf(mappingId));
                List<FieldMapping> fieldList = entityMappingService.addMappingWithXml(info, mapping, request, collist);
                wrapSuccess(retMap, "保存配置成功");
            } else {
                mapping = new EntityMapping();
                wrapObjectWithRequest(request, mapping);
                retMap.put("success", false);
                retMap.put("message", "请先配置实体");
            }

        } catch (Exception ex) {
            retMap.put("success", false);
            retMap.put("message", ex.getMessage());
        }
        return retMap;
    }

    @RequestMapping("genCode")
    @ResponseBody
    public Map<String, Object> genCode(HttpServletRequest request, HttpServletResponse response) {
        String schema = request.getParameter("schema");
        String projId = request.getParameter(COL_PROJID);
        String table = request.getParameter("table");
        DataBaseUtil util = null;
        Map<String, Object> retMap = new HashMap<>();
        try {
            ProjectInfo info = projectInfoService.getEntity(Long.valueOf(projId));
            DataSource source = dataSourceService.getEntity(info.getDataSourceId());
            String type = source.getDbType();
            List<Map<String, Object>> driverList = jdbcDao.queryBySql("select db_type as dbType,driver_class as driverClass from t_base_dbdriver where id=?", source.getDriverId());

            DataBaseParam param = new DataBaseParam(source.getHostIp(), Integer.parseInt(source.getPort()), source.getDatabaseName(), source.getUserName(), source.getPassword());
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(type, param);
            util = new DataBaseUtil();
            util.connect(meta);
            List<DataBaseColumnMeta> collist = util.getTableMetaByTableName(table, schema);

            String columnName = request.getParameter("columnName");
            EntityMapping enmap = entityMappingService.getEntity(mapping.getId());
            String pktype = findCodeName("PKTYPE", enmap.getPkType());
            enmap.setPkType(pktype);
            List<FieldMapping> fieldList = fieldMappingService.queryByField("entityId", BaseObject.OPER_EQ, mapping.getId());
            List<Map<String, String>> fieldmapList = convertObjToMapList(fieldList);
            boolean genDao = request.getParameter("genDao") != null && request.getParameter("genDao").equals(Const.VALID);

            String basePath = info.getProjBasePath();
            String className = enmap.getJavaClass().substring(0, 1).toUpperCase() + enmap.getJavaClass().substring(1, enmap.getJavaClass().length());
            Map<String, String> entityMap = new HashMap<>();
            ConvertUtil.objectToMap(entityMap, enmap);
            entityMap.put("pkType", pktype);
            entityMap.put("upperName", className);
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            String modelsrc = basePath + "src/main/java/" + enmap.getModelPackage().replaceAll("\\.", "/") + "/";
            String daosrc = basePath + "src/main/java/" + enmap.getDaoPackage().replaceAll("\\.", "/") + "/";
            FileUtils.forceMkdir(new File(modelsrc));
            FileUtils.forceMkdir(new File(daosrc));
            String modelfile = modelsrc + className + ".java";
            String daofile = daosrc + className + "Dao.java";
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("project", info);
            parammap.put("class", entityMap);
            parammap.put("source", source);
            parammap.put("driver", driverList.get(0));
            setCode("DBDISPLAY,PKTYPE");
            filterListByCodeSet(fieldmapList, "type", "DBDISPLAY");

            for (Map<String, String> stringStringMap : fieldmapList) {
                String name = stringStringMap.get("code");
                stringStringMap.put("uppername", name.substring(0, 1).toUpperCase() + name.substring(1, name.length()));
            }
            parammap.put("fields", fieldmapList);
            List<Map<String, Object>> templist = jdbcDao.queryBySql("select name,template_path as path from t_base_codetemplate");
            Map<String, List<Map<String, Object>>> tempmap = CollectionBaseConvert.convertToMapByParentKeyWithObjVal(templist, "name");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            parammap.put("CurYear", String.valueOf(calendar.get(Calendar.YEAR)));
            parammap.put("LastUpdate", format.format(calendar.getTime()));
            parammap.put("pkType", pktype);
            Map<String, String> projectConfig = projectInfoService.getTemplateNameByProject(info);
            FreeMarkerUtil freeutil = new FreeMarkerUtil(request.getSession().getServletContext(), "/template");
            String modeltype = projectConfig.get("modelFramePrefix");
            if (genDao) {
                generateCode(freeutil, modelfile, tempmap.get(modeltype + "modelclass").get(0).get("path").toString(), parammap);

                if ("hibernate".equals(modeltype)) {
                    generateCode(freeutil, daofile, tempmap.get(modeltype + "daoclass").get(0).get("path").toString(), parammap);
                    String mappingfile = modelsrc + className + ".hbm.xml";
                    generateCode(freeutil, mappingfile, tempmap.get(modeltype + "mapping").get(0).get("path").toString(), parammap);
                }
            }
            boolean genService = request.getParameter("genService") != null && request.getParameter("genService").equals(Const.VALID);
            if (genService) {
                String servicesrc = basePath + "src/main/java/" + enmap.getServicePackage().replaceAll("\\.", "/") + "/";
                FileUtils.forceMkdir(new File(servicesrc));
                String servicefile = servicesrc + className + "Service.java";
                generateCode(freeutil, servicefile, tempmap.get(modeltype + "serviceclass").get(0).get("path").toString(), parammap);
            }
            boolean genWeb = request.getParameter("genWeb") != null && request.getParameter("genWeb").equals(Const.VALID);
            if (genWeb) {
                String webpath = basePath + "src/main/webapp/";
                if (info.getWebBasePath() != null) {
                    webpath += info.getWebBasePath();
                }

                File file = new File(webpath);
                String webfrm = projectConfig.get("webFramePrefix");
                if (!file.exists()) {
                    String path = request.getSession().getServletContext().getRealPath(tempmap.get(webfrm + "initpack").get(0).get("path").toString());
                    CompressUtils.unzip(new File(path), new File(webpath));
                }
                String actionType = projectConfig.get("actionType");
                String actionsuffix = "mvc".equals(projectConfig.get("actionType")) ? "Contorller" : "Action";
                String actionsrc = basePath + "src/main/java/" + enmap.getWebPackage().replaceAll("\\.", "/") + "/";
                FileUtils.forceMkdir(new File(actionsrc));
                String actionfile = actionsrc + className + actionsuffix + ".java";
                generateCode(freeutil, actionfile, tempmap.get(actionType + "class").get(0).get("path").toString(), parammap);
                List<Map<String, String>> queryFieldList = new ArrayList<>();
                List<Map<String, String>> displayFieldList = new ArrayList<>();
                List<Map<String, String>> editList = new ArrayList<>();
                for (Map<String, String> stringStringMap : fieldmapList) {
                    if ("1".equals(stringStringMap.get("showInquery"))) {
                        queryFieldList.add(stringStringMap);
                    }
                    if ("1".equals(stringStringMap.get("showIngrid"))) {
                        displayFieldList.add(stringStringMap);
                    }
                    if ("1".equals(stringStringMap.get("editable"))) {
                        editList.add(stringStringMap);
                    }
                }
                parammap.put("queryfieldList", queryFieldList);
                parammap.put("displayFieldList", displayFieldList);
                parammap.put("fieldList", editList);
                String jspsrc = webpath + enmap.getPagePath();
                FileUtils.forceMkdir(new File(jspsrc));
                String addjsp = jspsrc + enmap.getJavaClass() + "_add.jsp";
                String listjsp = jspsrc + enmap.getJavaClass() + "_list.jsp";
                String editjsp = jspsrc + enmap.getJavaClass() + "_edit.jsp";
                generateCode(freeutil, addjsp, tempmap.get(webfrm + "addjsp").get(0).get("path").toString(), parammap);
                generateCode(freeutil, listjsp, tempmap.get(webfrm + "listjsp").get(0).get("path").toString(), parammap);
                generateCode(freeutil, editjsp, tempmap.get(webfrm + "editjsp").get(0).get("path").toString(), parammap);
            }
            wrapSuccess(retMap, "OK");
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        } finally {
            if (util != null) {
                util.closeConnection();
            }
        }
        return retMap;
    }

    public void generateCode(FreeMarkerUtil util, String filepath, String templateName, Map parammap) throws Exception {
        PrintWriter writer = new PrintWriter(new File(filepath));
        util.process(templateName, parammap, writer);
        writer.close();
    }

    public String showConfig(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("mapid");
        if (id != null && !"".equals(id)) {
            mapping = entityMappingService.getEntity(Long.valueOf(id));
        }
        setCode("PKGEN,PKTYPE,DATATYPE,YNTYPE,FIELDMAP");
        request.setAttribute("keyTypeList", findCodeSetArr("PKGEN"));
        request.setAttribute("pkTypeList", findCodeSetArr("PKTYPE"));
        request.setAttribute("ynList", findCodeSetArr("YNTYPE"));
        request.setAttribute("typeList", findCodeSetArr("FIELDMAP"));
        return "SHOWCFG";
    }

    @RequestMapping("/getConfig")
    @ResponseBody
    public EntityMapping getConfig(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        return entityMappingService.getEntity(Long.valueOf(id));
    }

    @RequestMapping("/getPkType")
    @ResponseBody
    public Map<String, Object> getPkType(HttpServletRequest request, HttpServletResponse response) {
        String allowNull = request.getParameter("allowNull");
        boolean insertNullVal = true;
        if (allowNull != null && !allowNull.isEmpty() && "false".equalsIgnoreCase(allowNull)) {
            insertNullVal = false;
        }
        return returnCodeSetDhtmlxCombo("PKTYPE", insertNullVal);
    }

    @RequestMapping("/getPkGen")
    @ResponseBody
    public Map<String, Object> getPkGenType(HttpServletRequest request, HttpServletResponse response) {
        String allowNull = request.getParameter("allowNull");
        boolean insertNullVal = true;
        if (allowNull != null && !allowNull.isEmpty() && "false".equalsIgnoreCase(allowNull)) {
            insertNullVal = false;
        }
        return returnCodeSetDhtmlxCombo("PKGEN", insertNullVal);
    }

    @RequestMapping("/saveConfig")
    @ResponseBody
    public Map<String, Object> saveConfig(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> retMap = new HashMap<>();
        mapping = new EntityMapping();
        try {
            ConvertUtil.mapToObject(mapping, wrapRequest(request));
            String javaClass = mapping.getJavaClass();
            if (mapping.getSpringName() == null || mapping.getSpringName().isEmpty()) {
                mapping.setSpringName(javaClass.substring(0, 1).toLowerCase() + javaClass.substring(1));
            }
            if (mapping.getId() != null && mapping.getId() != 0) {
                entityMappingService.updateEntity(mapping);
            } else {
                entityMappingService.saveEntity(mapping);
            }
            wrapSuccess(retMap, "OK");
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }


    public List<DataBaseColumnMeta> getFields() {
        return fields;
    }

    public void setFields(List<DataBaseColumnMeta> fields) {
        this.fields = fields;
    }

    public EntityMapping getMapping() {
        return mapping;
    }

    public void setMapping(EntityMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        return null;
    }
}
