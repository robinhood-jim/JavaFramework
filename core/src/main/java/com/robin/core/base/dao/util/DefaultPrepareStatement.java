package com.robin.core.base.dao.util;

import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.IUserUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.ObjectUtils;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class DefaultPrepareStatement implements PreparedStatementCreator {
    private String sql;
    private List<FieldContent> fields;
    private BaseObject object;
    private LobHandler lobHandler;
    private EntityMappingUtil.InsertSegment insertSegment;

    public DefaultPrepareStatement(List<FieldContent> fields, final String sql, BaseObject object, LobHandler lobHandler, EntityMappingUtil.InsertSegment insertSegment) {
        this.sql = sql;
        this.fields = fields;
        this.object = object;
        this.lobHandler=lobHandler;
        this.insertSegment=insertSegment;
    }

    @Override
    public java.sql.PreparedStatement createPreparedStatement(Connection conn)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int pos = 1;
        for (FieldContent field : fields) {
            pos = AnnotationRetriever.replacementPrepared(ps, lobHandler, field, object, pos,insertSegment);
        }
        if(object.isHasDefaultColumn()){
            if(!ObjectUtils.isEmpty(object.getCreateTimeColumn()) && !ObjectUtils.isEmpty(insertSegment.getColumnMetaMap().get(object.getCreateTimeColumn()))){
                ps.setTimestamp(pos++,new Timestamp(System.currentTimeMillis()));
            }
            if(!ObjectUtils.isEmpty(object.getUpdateTimeColumn()) && !ObjectUtils.isEmpty(insertSegment.getColumnMetaMap().get(object.getUpdateTimeColumn()))){
                ps.setTimestamp(pos++,new Timestamp(System.currentTimeMillis()));
            }
            if(!ObjectUtils.isEmpty(object.getCreatorColumn()) && !ObjectUtils.isEmpty(insertSegment.getColumnMetaMap().get(object.getCreatorColumn()))){
                IUserUtils utils= SpringContextHolder.getBean(IUserUtils.class);
                if(!ObjectUtils.isEmpty(utils) && !ObjectUtils.isEmpty(utils.getLoginUser())){
                    ps.setLong(pos,utils.getLoginId());
                }
            }
        }
        return ps;
    }
}