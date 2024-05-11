package com.robin.core.base.dao.util;

import com.robin.core.base.model.BaseObject;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class DefaultPrepareStatement implements PreparedStatementCreator {
    private String sql;
    private List<FieldContent> fields;
    private BaseObject object;
    private LobHandler lobHandler;

    public DefaultPrepareStatement(List<FieldContent> fields, final String sql, BaseObject object, LobHandler lobHandler) {
        this.sql = sql;
        this.fields = fields;
        this.object = object;
        this.lobHandler=lobHandler;
    }

    @Override
    public java.sql.PreparedStatement createPreparedStatement(Connection conn)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int pos = 1;
        for (FieldContent field : fields) {
            pos = AnnotationRetriever.replacementPrepared(ps, lobHandler, field, object, pos);
        }
        return ps;
    }
}