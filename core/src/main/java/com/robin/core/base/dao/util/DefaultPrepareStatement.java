package com.robin.core.base.dao.util;

import com.robin.core.base.model.BaseObject;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * <p>Created at: 2019-08-16 14:31:12</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class DefaultPrepareStatement implements PreparedStatementCreator {
    private String sql;
    private List<AnnotationRetrevior.FieldContent> fields;
    private BaseObject object;
    private LobHandler lobHandler;

    public DefaultPrepareStatement(List<AnnotationRetrevior.FieldContent> fields, final String sql, BaseObject object,LobHandler lobHandler) {
        this.sql = sql;
        this.fields = fields;
        this.object = object;
        this.lobHandler=lobHandler;
    }

    public java.sql.PreparedStatement createPreparedStatement(Connection conn)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int pos = 1;
        for (AnnotationRetrevior.FieldContent field : fields) {
            pos = AnnotationRetrevior.replacementPrepared(ps, lobHandler, field, object, pos);
        }
        return ps;
    }
}