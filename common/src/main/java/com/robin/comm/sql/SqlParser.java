package com.robin.comm.sql;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.sql</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年11月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class SqlParser {
    public static void main(String[] args){
        try {
            Statement statment = CCJSqlParserUtil.parse("insert into t_tset values('a',b,c)");
            Insert insert= (Insert) statment;
            System.out.println(insert.getTable());
            System.out.println(insert.getColumns());
            System.out.println(insert.getItemsList());

        }catch (Exception ex){

        }

    }
}
