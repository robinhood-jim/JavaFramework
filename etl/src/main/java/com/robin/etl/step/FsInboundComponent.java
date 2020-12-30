package com.robin.etl.step;

import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;

public class FsInboundComponent extends AbstractComponent {
    protected AbstractFileIterator iterator;


    @Override
    protected void init(StatefulJobContext context, StepContext stepContext) {
        super.init(context,stepContext);

    }

    @Override
    protected boolean prepare(String cycle) {
        try {
            jobContext.getInputMeta().setPath(parseProcessFsPath(cycle));
            iterator = TextFileIteratorFactory.getProcessIteratorByType(jobContext.getInputMeta());
        }catch (Exception ex){

        }
        return false;
    }

    @Override
    protected boolean finish(String cycle) {
        return false;
    }

    @Override
    protected boolean doOperation(String cycle) {
        return true;
    }
    public AbstractFileIterator getResourceIterator(){
        return iterator;
    }
}
