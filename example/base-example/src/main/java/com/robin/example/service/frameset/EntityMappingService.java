package com.robin.example.service.frameset;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.example.model.frameset.EntityMapping;
import com.robin.example.model.frameset.FieldMapping;
import com.robin.example.model.frameset.ProjectInfo;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.*;

@Component(value = "entityMappingService")
@Scope(value = "singleton")
public class EntityMappingService extends BaseAnnotationJdbcService<EntityMapping, Long> {
    @Autowired
    private JdbcDao jdbcDao;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public List<FieldMapping> addMapping(ProjectInfo info, EntityMapping mapping, HttpServletRequest request, List<DataBaseColumnMeta> metalist) throws ServiceException {
        List<FieldMapping> retList = new ArrayList<FieldMapping>();
        try {
            //ConvertUtil.convertToModel(mapping, paramMap);
            jdbcDao.deleteByField(FieldMapping.class, "entityId", mapping.getId());
            Long entityId = mapping.getId();
            String[] columnsArr = request.getParameterValues("columnName");
            String[] propNameArr = request.getParameterValues("propName");
            String[] propTypeArr = request.getParameterValues("propType");
            String[] colNameArr = request.getParameterValues("name");
            String[] displayArr = request.getParameterValues("displayType");
            String[] showingridArr = request.getParameterValues("showIngrid");
            String[] showinqueryArr = request.getParameterValues("showInquery");
            String[] editableArr = request.getParameterValues("editable");
            for (int i = 0; i < propTypeArr.length; i++) {
                FieldMapping field = new FieldMapping();
                field.setCode(propNameArr[i]);
                field.setEntityId(entityId);
                field.setDataType(metalist.get(i).getColumnType().toString());
                field.setProjId(info.getId());
                field.setSourceId(info.getDataSourceId());
                field.setIsPrimary(metalist.get(i).isPrimaryKey() ? "1" : "0");
                field.setIsGenkey(metalist.get(i).isIncrement() ? "1" : "0");
                field.setIsNull(metalist.get(i).isNullable() ? "1" : "0");
                if (metalist.get(i).isPrimaryKey() && "4".equals(mapping.getGenType())) {
                    field.setIsSequnce("1");
                    field.setSeqName(request.getParameter("sequenceName"));
                }
                field.setType(propTypeArr[i]);
                field.setField(columnsArr[i]);
                field.setName(colNameArr[i]);
                field.setDisplayType(displayArr[i]);
                field.setShowIngrid(showingridArr[i]);
                field.setShowInquery(showinqueryArr[i]);
                field.setEditable(editableArr[i]);
                Long fid = (Long)jdbcDao.createVO(field);
                field.setId(fid);
                retList.add(field);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("message", ex);
            throw new ServiceException(ex);
        }
        return retList;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public List<FieldMapping> addMappingWithXml(ProjectInfo info, EntityMapping mapping, HttpServletRequest request, List<DataBaseColumnMeta> metalist) throws ServiceException {
        List<FieldMapping> retList = new ArrayList<FieldMapping>();
        Document doc = null;
        SAXReader reader = new SAXReader();
        try {
            if (mapping.getId() == null) {
                jdbcDao.createVO(mapping);
            } else {
                jdbcDao.updateVO(EntityMapping.class, mapping);
            }
            jdbcDao.deleteByField(FieldMapping.class, "entityId", mapping.getId());
            Long entityId = mapping.getId();
            doc = reader.read(new ByteArrayInputStream(request.getParameter("gridinput").getBytes("UTF-8")));
            Element ele = doc.getRootElement();
            Iterator iter = ele.elementIterator("row");
            String[] keyArr = {"columnName", "columnType", "propName", "propType", "name", "displayType", "showIngrid", "showInquery", "editable"};
            int pos = 0;
            while (iter.hasNext()) {
                Map<String, String> vmap = new HashMap<String, String>();
                Element element = (Element) iter.next();
                String id = element.attributeValue("id");
                List eleList = element.elements();
                for (int i = 0; i < eleList.size(); i++) {
                    vmap.put(keyArr[i], ((Element) eleList.get(i)).getStringValue());
                }
                retList.add(saveMapping(vmap, entityId, metalist.get(pos++), info, mapping, request));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("message", ex);
            throw new ServiceException(ex);
        }
        return retList;
    }

    private FieldMapping saveMapping(Map<String, String> map, Long entityId, DataBaseColumnMeta meta, ProjectInfo info, EntityMapping mapping, HttpServletRequest request) {
        FieldMapping field = new FieldMapping();
        field.setCode(map.get("propName"));
        field.setEntityId(entityId);
        field.setDataType(meta.getColumnType().toString());
        field.setProjId(info.getId());
        field.setSourceId(info.getDataSourceId());
        field.setIsPrimary(meta.isPrimaryKey() ? "1" : "0");
        field.setIsGenkey(meta.isIncrement() ? "1" : "0");
        field.setIsNull(meta.isNullable() ? "1" : "0");
        if (meta.isPrimaryKey() && "4".equals(mapping.getGenType())) {
            field.setIsSequnce("1");
            field.setSeqName(request.getParameter("sequenceName"));
        }
        field.setType(map.get("propType"));
        field.setField(map.get("columnName"));
        field.setName(map.get("name"));
        field.setDisplayType(map.get("displayType"));
        field.setShowIngrid(map.get("showIngrid"));
        field.setShowInquery(map.get("showInquery"));
        field.setEditable(map.get("editable"));
        Long fid = (Long)jdbcDao.createVO(field);
        field.setId(fid);
        return field;
    }


}
