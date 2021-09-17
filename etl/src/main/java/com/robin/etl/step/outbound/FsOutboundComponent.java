package com.robin.etl.step.outbound;

import com.robin.etl.step.AbstractComponent;

public class FsOutboundComponent extends AbstractComponent {

    public FsOutboundComponent(Long stepId) {
        super(stepId);
    }

    @Override
    public boolean prepare(String cycle) {
        return false;
    }

    @Override
    public boolean finish(String cycle) {
        return false;
    }

    //@Override
    public Integer doExecute() {
        return null;
    }
}
