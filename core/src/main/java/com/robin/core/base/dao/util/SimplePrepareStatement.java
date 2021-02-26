package com.robin.core.base.dao.util;

import com.robin.core.base.model.BaseObject;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class SimplePrepareStatement implements PreparedStatementCreator {
    private String sql;
    private Object[] objects;
    private LobHandler lobHandler;
    public SimplePrepareStatement(String sql,Object[] objects,LobHandler lobHandler){
        this.sql=sql;
        this.objects=objects;
        this.lobHandler=lobHandler;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
        try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < objects.length; i++) {
                ps.setObject(i + 1, objects[i]);
            }
            return ps;
        }
    }
}
