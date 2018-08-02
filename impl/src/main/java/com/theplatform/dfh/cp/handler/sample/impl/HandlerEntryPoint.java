package com.theplatform.dfh.cp.handler.sample.impl;

import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.field.translator.api.FieldTranslator;
import com.theplatform.dfh.cp.handler.field.translator.json.JsonFieldTranslator;
import com.theplatform.dfh.cp.handler.reporter.api.ReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;

public class HandlerEntryPoint
{
    private ReporterSet reporterSet = new KubernetesReporterSet();
    private JsonFieldTranslator fieldTranslator;

    public HandlerEntryPoint(JsonFieldTranslator fieldTranslator)
    {
        this.fieldTranslator = fieldTranslator;
    }

    public static void main(String[] args)
    {
        new HandlerEntryPoint(new JsonFieldTranslator(new EnvironmentFieldRetriever())).execute();
    }

    public void execute()
    {
        SampleInput sampleInput = fieldTranslator.getObject(HandlerField.PAYLOAD, SampleInput.class);

        reporterSet.reportProgress(sampleInput);
        reporterSet.reportProgress(sampleInput);
        reporterSet.reportSuccess("All Done!");
    }

    public void setReporterSet(ReporterSet reporterSet)
    {
        this.reporterSet = reporterSet;
    }
}
