package com.robin.etl.step.inbound;

import com.robin.comm.fileaccess.fs.CloudStorageFileSystemAccessorFactory;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.etl.common.EtlConstant;
import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;
import com.robin.etl.step.AbstractComponent;

public class FsInboundComponent extends AbstractComponent {
    protected AbstractFileIterator iterator;
    protected AbstractFileSystemAccessor accessor;
    protected String fileStorageType;

    public FsInboundComponent(Long stepId) {
        super(stepId);
    }


    @Override
    protected void init(StatefulJobContext context, StepContext stepContext) {
        super.init(context,stepContext);
        if(context.getJobParam().containsKey(EtlConstant.FILESTORAGETYPE)){
            fileStorageType=context.getJobParam().get(EtlConstant.FILESTORAGETYPE).toString();
            accessor= FileSystemAccessorFactory.getResourceAccessorByType(fileStorageType);
            if (accessor == null) {
                accessor= CloudStorageFileSystemAccessorFactory.getAccessorByIdentifier(context.getInputMeta(),fileStorageType);
            }
        }

    }
    public void withFilterSql(String sql){
        if(iterator!=null){
            iterator.withFilterSql(sql);
        }
    }

    @Override
    public boolean prepare(String cycle) {
        try {
            jobContext.getInputMeta().setPath(parseProcessFsPath(cycle));
            iterator =(AbstractFileIterator) TextFileIteratorFactory.getProcessIteratorByType(jobContext.getInputMeta(),accessor);
        }catch (Exception ex){

        }
        return false;
    }

    @Override
    public boolean finish(String cycle) {
        try {
            if (null != iterator) {
                iterator.close();
            }
        }catch (Exception ex){

            return false;
        }
        return true;
    }

    public IResourceIterator getResourceIterator(){
        return iterator;
    }


}
