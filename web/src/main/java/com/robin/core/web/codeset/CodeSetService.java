package com.robin.core.web.codeset;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.web.codeset.Code;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CodeSetService {
    @Cacheable(value = "codeSetCache",key = "#codeSetNo")
    public List<Code> getCacheCode(String codeSetNo) throws DAOException {
        return getCodeSetDefault(codeSetNo);
    }
    protected List<Code> getCodeSetDefault(String codeSetNo){
        JdbcDao jdbcDao= (JdbcDao) SpringContextHolder.getBean(JdbcDao.class);
        QueryFactory factory= (QueryFactory) SpringContextHolder.getBean(QueryFactory.class);
        List<Code> codeList=new ArrayList<>();
        if(factory.isSelectIdExists("$_GETCODESET")){
            PageQuery query=new PageQuery();
            query.setPageSize("0");
            query.setSelectParamId("$_GETCODESET");
            query.setParameterArr(new Object[]{codeSetNo});
            jdbcDao.queryBySelectId(query);
            if(!query.getRecordSet().isEmpty()){
                for(Map<String,Object> map:query.getRecordSet()){
                    codeList.add(new Code(map.get("ITEMNAME").toString(),map.get("ITEMVALUE").toString()));
                }
            }
            return codeList;
        }else{
            throw new DAOException(" Query Parameter $_GETCODESET not config,Please config queryConfig xml");
        }
    }


    @CachePut(value = "codeSetCache",key = "#codeSetNo")
    public List<Code> setCode(String codeSetNo, List<?> codes, String label, String value)
    {
        if (codes == null) {
            return null;
        }
        List<Code> al = new ArrayList();
        for (int i = 0; i < codes.size(); i++)
        {
            Code code = new Code();
            Object objtmp = codes.get(i);
            Object ol ;
            Object ov;
            try
            {
                ol = PropertyUtils.getProperty(objtmp, label);
                ov = PropertyUtils.getProperty(objtmp, value);
            }
            catch (Exception e)
            {
                ol = null;
                ov = null;
            }
            if ((ol != null) && (ov != null))
            {
                code.setCodeName(ol.toString());
                code.setValue(ov.toString());
                al.add(code);
            }
        }
        return al;
    }
}
