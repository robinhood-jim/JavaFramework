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
package com.robin.core.fileaccess.writer;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class TextFileWriterFactory {
    private static Logger logger = LoggerFactory.getLogger(TextFileWriterFactory.class);

    public static AbstractFileWriter getFileWriterByType(DataCollectionMeta colmeta, BufferedWriter writer) throws IOException {
        AbstractFileWriter fileWriter = getFileWriterByType(colmeta);
        fileWriter.setWriter(writer);
        return fileWriter;
    }

    public static AbstractFileWriter getFileOutputStreamByType(DataCollectionMeta colmeta, OutputStream writer) throws IOException{
        AbstractFileWriter fileWriter = getFileWriterByType(colmeta);
        if(writer!=null) {
            fileWriter.setOutputStream(writer);
        }
        return fileWriter;
    }

    public static AbstractFileWriter getFileWriterByPath(DataCollectionMeta colmeta, OutputStream writer) throws IOException{
        List<String> suffixList=new ArrayList<String>();
        FileUtils.parseFileFormat(colmeta.getPath(),suffixList);
        String fileFormat=suffixList.get(0);
        AbstractFileWriter fileWriter = getFileWriterByType(colmeta);
        fileWriter.setOutputStream(writer);
        return fileWriter;
    }

    private static AbstractFileWriter getFileWriterByType(DataCollectionMeta colmeta) throws IOException {
        AbstractFileWriter fileWriter = null;
        try {
            String fileSuffix=colmeta.getFileFormat();
            if(fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_CSV)){
                fileWriter=new PlainTextFileWriter(colmeta);
            } else if (fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_JSON)) {
                fileWriter = new JsonFileWriter(colmeta);
            } else if (fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_XML)) {
                fileWriter = new XmlFileWriter(colmeta);
            } else if (fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_AVRO)) {
                Class<AbstractFileWriter> clazz=(Class<AbstractFileWriter>) Class.forName(Const.FILEWRITER_AVRO_CLASSNAME);
                fileWriter = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            } else if (fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_PARQUET)) {
                Class<AbstractFileWriter> clazz = (Class<AbstractFileWriter>) Class.forName(Const.FILEWRITER_PARQUET_CLASSNAME);
                fileWriter = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            } else if (fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_PROTOBUF)) {
                Class<AbstractFileWriter> clazz = (Class<AbstractFileWriter>) Class.forName(Const.FILEWRITER_PROTOBUF_CLASSNAME);
                fileWriter = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            }else if(fileSuffix.equalsIgnoreCase(Const.FILESUFFIX_ORC)){
                Class<AbstractFileWriter> clazz = (Class<AbstractFileWriter>) Class.forName(Const.FILEWRITER_ORC_CLASSNAME);
                fileWriter = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            }

            else {
                fileWriter = new PlainTextFileWriter(colmeta);
            }

        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return fileWriter;
    }

}
