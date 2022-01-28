package com.robin.etl.step;

/**
 * using Apache Beam as etl process engine
 */
public abstract class AbstractBeamComponent extends AbstractComponent {

    protected AbstractBeamComponent(Long stepId) {
        super(stepId);
    }
}
