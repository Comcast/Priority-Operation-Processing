package com.comcast.fission.handler.sample.impl.progress;

public class SampleJobInfo
{
    public final static String PARAM_NAME = "jobInfo";

    private String jobId;

    public SampleJobInfo()
    {
    }

    public SampleJobInfo(String jobId)
    {
        this.jobId = jobId;
    }

    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }
}
