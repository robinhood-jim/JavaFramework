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

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ClassGraphReflector;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.SqlDialectFactory;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class ModelScriptGenerator {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("usage java -classpath .:./* com.robin.core.base.util.ModelScriptGenerator %DBTYPE% %OUTPUT_FILE%");
            System.exit(-1);
        }
        String configFile = args[0];
        String outputFile = args[1];

    }
    public static void generateScript(String dbType, BaseDataBaseMeta meta,String outputFile, String... packageNames){

        try (BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"))){

            ClassGraphReflector reflector= new ClassGraphReflector();
            if(packageNames.length>0){
                reflector.setScanPackage(packageNames[0]);
            }
            reflector.afterPropertiesSet();
            ClassInfoList classes=reflector.getAnnotationClasses(MappingEntity.class);

            BaseSqlGen sqlgen = SqlDialectFactory.getSqlGeneratorByDialect(dbType);
            //final Set<String> clazzNames = db.getAnnotationIndex().get(MappingEntity.class.getName());

            StringBuilder builder = new StringBuilder();
            for(ClassInfo classInfo:classes){
                if(classInfo.getSuperclass().loadClass().isAssignableFrom(BaseObject.class) || classInfo.getSuperclass().loadClass().getSuperclass().isAssignableFrom(BaseObject.class)) {
                    builder.append(ModelSqlGenerator.generateCreateSql((Class<? extends BaseObject>) classInfo.loadClass(),meta, sqlgen));
                }
            }
            writer.write(builder.toString());
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
