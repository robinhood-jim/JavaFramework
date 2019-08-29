package com.robin.core.web.codeset;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CodeSetService {
    @Cacheable(value = "codeSetCache", key = "#codeSetNo")
    public Map<String,String> getCacheCode(String codeSetNo) throws DAOException {
        return getCodeSetDefault(codeSetNo);
    }


    protected Map<String,String> getCodeSetDefault(String codeSetNo) {
        JdbcDao jdbcDao = SpringContextHolder.getBean(JdbcDao.class);
        QueryFactory factory = SpringContextHolder.getBean(QueryFactory.class);
        Map<String,String> codeMap=new HashMap<>();
        if (factory.isSelectIdExists("$_GETCODESET")) {
            PageQuery query = new PageQuery();
            query.setPageSize("0");
            query.setSelectParamId("$_GETCODESET");
            query.setParameterArr(new Object[]{codeSetNo});
            jdbcDao.queryBySelectId(query);
            if (!query.getRecordSet().isEmpty()) {
                for (Map<String, Object> map : query.getRecordSet()) {
                    codeMap.put(map.get("ITEMVALUE").toString(),map.get("ITEMNAME").toString());
                }
            }
            return codeMap;
        } else {
            throw new DAOException(" Query Parameter $_GETCODESET not config,Please config queryConfig xml");
        }
    }


    @Cacheable(value = "codeSetCache", key = "#codeSetNo")
    public Map<String,String> setCode(String codeSetNo, List<?> codes, String label, String value) {
        if (codes == null) {
            return null;
        }
        Map<String,String> map=new HashMap<>();
        for (int i = 0; i < codes.size(); i++) {
            Object objtmp = codes.get(i);
            Object ol;
            Object ov;
            try {
                ol = BeanUtils.getProperty(objtmp, label);
                ov = BeanUtils.getProperty(objtmp, value);
            } catch (Exception e) {
                ol = null;
                ov = null;
            }
            if ((ol != null) && (ov != null)) {
                map.put(ov.toString(),ol.toString());
                //code.setCodeName(ol.toString());
                //code.setValue(ov.toString());
            }
        }
        return map;
    }
}
