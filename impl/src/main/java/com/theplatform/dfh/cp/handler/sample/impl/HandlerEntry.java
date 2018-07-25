package com.theplatform.dfh.cp.handler.sample.impl;

import com.theplatform.dfh.cp.handler.base.HandlerEntryBase;
import com.theplatform.dfh.cp.handler.reporter.log.ReporterLogger;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;

public class HandlerEntry extends HandlerEntryBase<SampleInput>
{
    public static void main(String[] args)
    {
        new HandlerEntry().entryPoint(args);
    }

    @Override
    public void execute(SampleInput inputObject)
    {
        getReporters().add(new ReporterLogger());
        getReporters().reportProgress(inputObject);
        getReporters().reportProgress(inputObject);
        getReporters().reportSuccess("All Done!");
    }

    @Override
    protected Class getInputObjectClass()
    {
        return SampleInput.class;
    }
}
