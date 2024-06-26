package com.robin.etl.step.inbound;

import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;
import com.robin.etl.step.AbstractComponent;

public class FsInboundComponent extends AbstractComponent {
    protected IResourceIterator iterator;

    public FsInboundComponent(Long stepId) {
        super(stepId);
    }


    @Override
    protected void init(StatefulJobContext context, StepContext stepContext) {
        super.init(context,stepContext);

    }

    @Override
    public boolean prepare(String cycle) {
        try {
            jobContext.getInputMeta().setPath(parseProcessFsPath(cycle));
            iterator = TextFileIteratorFactory.getProcessIteratorByType(jobContext.getInputMeta());
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
