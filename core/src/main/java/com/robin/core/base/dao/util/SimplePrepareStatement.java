package com.robin.core.base.dao.util;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.sql.*;


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
                if(!ObjectUtils.isEmpty(objects[i])) {
                    if (byte[].class.isAssignableFrom(objects[i].getClass())) {
                        byte[] bytes = (byte[]) objects[i];
                        lobHandler.getLobCreator().setBlobAsBinaryStream(ps, i + 1, new ByteArrayInputStream(bytes), bytes.length);
                    }
                    if (String.class.isAssignableFrom(objects[i].getClass())) {
                        if(objects[i].toString().length()>512){
                            lobHandler.getLobCreator().setClobAsString(ps,i+1,objects[i].toString());
                        }else{
                            ps.setString(i+1,objects[i].toString());
                        }
                    } else {
                        ps.setObject(i + 1, objects[i]);
                    }
                }else{
                    ps.setNull(i+1, Types.VARCHAR);
                }
            }
            return ps;
        }
    }
}
