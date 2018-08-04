package com.theplatform.dfh.cp.handler.sample.impl;

import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
import com.theplatform.dfh.cp.jsonhelper.JsonHelper;

public class HandlerEntryPoint
{
    private ReporterSet reporterSet = new KubernetesReporterSet();
    private LaunchDataWrapper launchDataWrapper;
    private JsonHelper jsonHelper;

    public HandlerEntryPoint(String[] args)
    {
        launchDataWrapper = new DefaultLaunchDataWrapper(args);
        jsonHelper = new JsonHelper();
    }

    /**
     * Local/IDEA non-docker execution prerequisites:
     * - debugging/running with a local-only build use these args (will definitely need to adjust the payload.json accordingly):
     * -launchType local -propFile ./handler/main/package/local/config/external.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        // just for convenience...
        System.out.println(System.getProperty("user.dir"));

        new HandlerEntryPoint(args).execute();
    }

    public void execute()
    {
        SampleInput sampleInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), SampleInput.class);

        reporterSet.reportProgress(sampleInput);
        reporterSet.reportProgress(sampleInput);
        reporterSet.reportSuccess("All Done!");
    }

    public void setReporterSet(ReporterSet reporterSet)
    {
        this.reporterSet = reporterSet;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
