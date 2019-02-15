/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.base.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.model.BaseObject;
import com.robin.core.sql.util.BaseSqlGen;

public class ModelScriptGenerator {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("usage java -classpath .:./* com.robin.core.base.util.ModelScriptGenerator %SPRINGCFGFILE% %OUTPUT_FILE%");
            System.exit(-1);
        }
        String configFile = args[0];
        String outputFile = args[1];
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        BufferedWriter writer = null;
        try {
            URL[] urls = ClasspathUrlFinder.findClassPaths();
            AnnotationDB db = new AnnotationDB();
            db.scanArchives(urls[0]);
            BaseSqlGen sqlgen = context.getBean(BaseSqlGen.class);
            final Set<String> clazzNames = db.getAnnotationIndex().get(MappingEntity.class.getName());
            Iterator<String> iter = clazzNames.iterator();
            writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            StringBuilder builder = new StringBuilder();
            while (iter.hasNext()) {
                builder.append(generateCreateSql(iter.next(),sqlgen));
            }
            writer.write(builder.toString());
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            ((ClassPathXmlApplicationContext) context).close();
        }
    }
    public static String generateCreateSql(String clazzName,BaseSqlGen sqlGen) throws Exception{
        StringBuilder builder=new StringBuilder();
        String name="";
        Map<String, String> tableMap = new HashMap<String, String>();
        BaseObject obj = (BaseObject) Class.forName(clazzName).newInstance();
        List<Map<String, Object>> fields = AnnotationRetrevior.getMappingFields(obj, tableMap, true);
        Map<String, Object> primarycol=AnnotationRetrevior.getPrimaryField(fields);
        builder.append("create table ");
        if (tableMap.containsKey("schema"))
            builder.append(tableMap.get("schema")).append(".");
        builder.append(tableMap.get("tableName")).append("(").append("\n");
        for (Map<String, Object> field : fields) {
            if (field.get("datatype") != null) {
                builder.append("\t").append(sqlGen.getCreateFieldPart(field).toLowerCase()).append(",\n");
            }
            if(field.get("increment")!=null){

            }
        }
        if(primarycol!=null && !primarycol.isEmpty()){
            name=primarycol.get("field").toString();
            if(name==null || name.isEmpty()){
                name=primarycol.get("name").toString();
            }
            builder.append("\tPRIMARY KEY(").append(name).append(")");
        }else
            builder.deleteCharAt(builder.length() - 2);
        builder.append(");\n");
        return builder.toString();
    }
}
