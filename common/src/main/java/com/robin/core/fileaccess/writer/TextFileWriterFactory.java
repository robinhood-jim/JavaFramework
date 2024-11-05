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

import com.robin.core.base.util.FileUtils;
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

    public static IResourceWriter getWriterByType(DataCollectionMeta colmeta, BufferedWriter writer) throws IOException {
        IResourceWriter fileWriter = getWriterByType(colmeta);
        fileWriter.setWriter(writer);
        return fileWriter;
    }

    public static IResourceWriter getOutputStreamByType(DataCollectionMeta colmeta, OutputStream writer) throws IOException{
        IResourceWriter fileWriter = getWriterByType(colmeta);
        if(writer!=null) {
            fileWriter.setOutputStream(writer);
        }
        return fileWriter;
    }

    public static IResourceWriter getWriterByPath(DataCollectionMeta colmeta, OutputStream writer) throws IOException{
        IResourceWriter fileWriter = getWriterByType(colmeta);
        fileWriter.setOutputStream(writer);
        return fileWriter;
    }

    public static IResourceWriter getWriterByType(DataCollectionMeta colmeta) throws IOException {
        IResourceWriter fileWriter = null;
        try {

            String fileSuffix=colmeta.getFileFormat();
            Class<? extends IResourceWriter> writerClass=fileWriterMap.get(fileSuffix);
            if (!ObjectUtils.isEmpty(writerClass)) {
                fileWriter =  writerClass.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
                logger.info("using resource writer {}",writerClass.getCanonicalName());
            }

        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return fileWriter;
    }
    private static void discoverIterator(Map<String,Class<? extends IResourceWriter>> fileIterMap){
        ServiceLoader.load(IResourceWriter.class).iterator().forEachRemaining(i->{
            if(AbstractFileWriter.class.isAssignableFrom(i.getClass()))
                fileIterMap.put(i.getIdentifier(),i.getClass());});
    }

}
