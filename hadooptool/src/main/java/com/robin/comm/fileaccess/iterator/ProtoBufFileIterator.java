package com.robin.comm.fileaccess.iterator;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.function.Abs;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
public class ProtoBufFileIterator extends AbstractFileIterator {

    private DynamicMessage message;


    private ProtoBufUtil.ProtoContainer container;
    public ProtoBufFileIterator(){
        identifier= Const.FILEFORMATSTR.PROTOBUF.getValue();
    }
    public ProtoBufFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.PROTOBUF.getValue();
    }
    public ProtoBufFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.PROTOBUF.getValue();
        this.accessUtil=accessor;
    }


    @Override
    public void beforeProcess() {
        try {
            container=ProtoBufUtil.initSchema(colmeta);

            checkAccessUtil(null);
            instream = accessUtil.getInResourceByStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }
    /*public static  ExtensionRegistry getExtension(DynamicSchema schema, DataCollectionMeta colmeta){
        ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        for(Descriptors.FieldDescriptor descriptor:schema.getMessageDescriptor(colmeta.getValueClassName()).getFields()){
            log.info("extension {}",descriptor.isExtension());
            extensionRegistry.add(descriptor);
        }
        return extensionRegistry;
    }*/

    @Override
    protected void pullNext() {
        try {
            cachedValue.clear();
            if (container.getMesgBuilder().mergeDelimitedFrom(instream)) {
                message = container.getMesgBuilder().build();
            } else {
                message = null;
            }
            if (message == null) {
                throw new NoSuchElementException("");
            }
            for (Descriptors.FieldDescriptor descriptor : container.getSchema().getMessageDescriptor(colmeta.getValueClassName()).getFields()) {
                cachedValue.put(descriptor.getName(), message.getField(descriptor));
            }

        } catch (Exception ex) {
            logger.error("{}", ex);
        }
    }


    public boolean hasNext1() {
        try {
            if (container.getMesgBuilder().mergeDelimitedFrom(instream)) {
                message = container.getMesgBuilder().build();
                return true;
            } else {
                message = null;
                return false;
            }
        } catch (Exception ex) {
            logger.error("{}", ex);
        }
        return false;
    }


    public Map<String, Object> next1() {
        if (message == null) {
            throw new NoSuchElementException("");
        }
        Map<String, Object> tmap = new HashMap<String, Object>();
        for (Descriptors.FieldDescriptor descriptor : container.getSchema().getMessageDescriptor(colmeta.getValueClassName()).getFields()) {
            tmap.put(descriptor.getName(), message.getField(descriptor));
        }
        return tmap;
    }

    @Override
    public void remove() {
        try {
            if (container.getMesgBuilder().mergeDelimitedFrom(instream, null)) {
                container.getMesgBuilder().build();
            }
        } catch (Exception ex) {

        }
    }
}
