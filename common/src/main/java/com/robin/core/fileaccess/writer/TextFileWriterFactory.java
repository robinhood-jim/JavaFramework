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
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


public class TextFileWriterFactory {
    private static Logger logger = LoggerFactory.getLogger(TextFileWriterFactory.class);
    private static Map<String,Class<? extends IResourceWriter>> fileWriterMap =new HashMap<>();
    static {
        discoverIterator(fileWriterMap);
    }

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
            Class<? extends IResourceWriter> writerClass=fileWriterMap.get(fileSuffix);
            if (!ObjectUtils.isEmpty(writerClass)) {
                fileWriter = (AbstractFileWriter) writerClass.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            }

        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return fileWriter;
    }
    private static void discoverIterator(Map<String,Class<? extends IResourceWriter>> fileIterMap){
        ServiceLoader.load(IResourceWriter.class).iterator().forEachRemaining(i->{if(i.getClass().isAssignableFrom(AbstractFileWriter.class))
                fileIterMap.put(i.getIdentifier(),i.getClass());});
    }

}
