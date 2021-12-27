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
package com.robin.comm.dal.holder;

import com.robin.core.base.exception.OperationInWorkException;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

@Slf4j
public class FsRecordIteratorHolder extends AbstractResourceHolder {
    protected AbstractFileIterator iterator;

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {
        if(iterator!=null || busyTag){
            throw new OperationInWorkException("last Opertaion reader already Exists.May not be shutdown Propery");
        }
        URI uri=new URI(colmeta.getPath());
        String schema=uri.getScheme();
        String path=uri.getPath();
        AbstractResourceAccessUtil util = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
        InputStream inputStream = util.getInResourceByStream(colmeta, path);
        iterator = TextFileIteratorFactory.getProcessIteratorByPath(colmeta, inputStream);
    }


    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Map<String, Object> getRecords() {
        return iterator.next();
    }

    @Override
    public void close() throws IOException {
        try {
            if (iterator != null) {
                iterator.close();
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            iterator = null;
            setBusyTag(false);
        }
    }


}
