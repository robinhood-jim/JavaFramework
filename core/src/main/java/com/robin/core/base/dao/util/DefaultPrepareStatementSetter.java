package com.robin.core.base.dao.util;

import com.robin.core.base.model.BaseObject;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>Created at: 2019-08-16 14:29:55</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class DefaultPrepareStatementSetter implements PreparedStatementSetter {
    private String sql;
    private List<AnnotationRetrevior.FieldContent> fields;
    private BaseObject object;

    public DefaultPrepareStatementSetter(List<AnnotationRetrevior.FieldContent> fields, final String sql, BaseObject object) {
        this.sql = sql;
        this.fields = fields;
        this.object = object;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        int pos = 1;
        try {
            for (AnnotationRetrevior.FieldContent field : fields) {
                Object value = field.getGetMethod().invoke(object, new Object[]{});
                if (!field.isIncrement() && value != null) {
                    AnnotationRetrevior.setParameter(ps, pos, value);
                    pos++;
                }
            }
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
}
