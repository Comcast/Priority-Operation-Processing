package com.theplatform.dfh.cp.handler.sample.impl;

import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.field.translator.json.JsonFieldTranslator;
import com.theplatform.dfh.cp.handler.reporter.api.ReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;

public class HandlerEntryPoint
{
    private ReporterSet reporterSet = new KubernetesReporterSet();

    public static void main(String[] args)
    {
        JsonFieldTranslator fieldTranslator = new JsonFieldTranslator(new EnvironmentFieldRetriever(), SampleInput.class);
        // TODO consider the importance of the getPayloadObject method...
        new HandlerEntryPoint().execute(fieldTranslator.getPayloadObject());
        // VS.
        new HandlerEntryPoint().execute(fieldTranslator.getObject(HandlerField.PAYLOAD, SampleInput.class));
    }

    public void execute(SampleInput inputObject)
    {
        reporterSet.reportProgress(inputObject);
        reporterSet.reportProgress(inputObject);
        reporterSet.reportSuccess("All Done!");
    }
}
