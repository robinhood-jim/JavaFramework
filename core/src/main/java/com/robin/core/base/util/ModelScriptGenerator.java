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
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ClassGraphReflector;
import com.robin.core.sql.util.BaseSqlGen;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.*;

public class ModelScriptGenerator {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("usage java -classpath .:./* com.robin.core.base.util.ModelScriptGenerator %SPRINGCFGFILE% %OUTPUT_FILE%");
            System.exit(-1);
        }
        String configFile = args[0];
        String outputFile = args[1];
        genrateScript(configFile,outputFile);
    }
    public static void genrateScript(String configFile,String outputFile){
        ApplicationContext context = null;

        BufferedWriter writer = null;
        try {
            if(configFile.startsWith("classpath:")){
                context=new ClassPathXmlApplicationContext(configFile);
            }else{
                context=new FileSystemXmlApplicationContext(configFile);
            }
            ClassGraphReflector reflector=context.getBean(ClassGraphReflector.class);
            ClassInfoList classes=reflector.getAnnotationClasses(MappingEntity.class);

            BaseSqlGen sqlgen = context.getBean(BaseSqlGen.class);
            //final Set<String> clazzNames = db.getAnnotationIndex().get(MappingEntity.class.getName());
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
            StringBuilder builder = new StringBuilder();
            for(ClassInfo classInfo:classes){
                if(classInfo.getSuperclass().loadClass().equals(BaseObject.class)) {
                    builder.append(ModelSqlGenerator.generateCreateSql((Class<? extends BaseObject>) classInfo.loadClass(), sqlgen));
                }
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
            //((ApplicationContext) context).close();
        }
    }


}
