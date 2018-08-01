package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.api.params.CredentialParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.params.TextParamKey;
import com.theplatform.dfh.cp.api.params.VideoParamKey;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class JsonUnmarshallingTest
{

    @Test
    public void testJsonToSources() throws Exception
    {
        TransformJob job = (TransformJob)JsonUtil.toObject(getStringFromResourceFile("Job.json"), TransformJob.class);
        Assert.assertNotNull(job);
        List<FileResource> videoSources = job.getResourcesByType(FileResourceType.MEZZANINE.getLabel());
        List<FileResource> textSources = job.getResourcesByType(FileResourceType.TEXT_TRACK_SIDECAR.getLabel());
        Assert.assertNotNull(videoSources);
        verifyVideo(videoSources.get(0), "/mount/path/filename.mov", "myid", "mypassword");
        verifyText(textSources.get(0), "/mount/path/filename-en.srt", "myid", "mypassword");
        verifyText(textSources.get(1), "/mount/path/filename-es.srt", "myid", "mypassword");
        Assert.assertNotNull(job.getInputStreams().getAudio().get(0).getParams().get("trackId"));
        Assert.assertNotNull(job.getInputStreams().getAudio().get(0).getInputRef());
        Assert.assertEquals(job.getInputStreams().getAudio().get(0).getParams().get("trackId"), new Integer(0));
        Assert.assertNotNull(job.getInputStreams());
        Assert.assertNotNull(job.getOutputStreams());
        verifyVideo(job.getOutputStreams().getVideo().get(0),  1920, 1020, 8000000L);
        verifyVideo(job.getOutputStreams().getVideo().get(1),  1280, 720, 2400000L);
    }

    private void verifyVideo(FileResource source, String expectedURL, String expectedUsername, String expectedPassword)
    {
        verifySource(source, expectedURL, expectedUsername, expectedPassword);
    }
    private void verifyText(FileResource source, String expectedURL, String expectedUsername, String expectedPassword)
    {
        Assert.assertNotNull(source.getParams().get(TextParamKey.intent));
        verifySource(source, expectedURL, expectedUsername, expectedPassword);
    }
    private void verifyVideo(OutputStream dataObject, Integer width, Integer height, Long bitrate)
    {
        Assert.assertNotNull(dataObject);
        ParamsMap params = dataObject.getParams();
        Assert.assertEquals(params.getLong(VideoParamKey.bitrate), bitrate);
        Assert.assertEquals(params.getInt(VideoParamKey.width), width);
        Assert.assertEquals(params.getInt(VideoParamKey.height), height);
    }
    private void verifySource(FileResource source, String expectedURL, String expectedUsername, String expectedPassword)
    {
       Assert.assertNotNull(source);
       Assert.assertEquals(source.getUrl(), expectedURL);
       Assert.assertEquals(source.getCredentials().get(CredentialParamKey.username), expectedUsername);
       Assert.assertEquals(source.getCredentials().get(CredentialParamKey.password), expectedPassword);
    }
    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
                this.getClass().getResource(file),
                "UTF-8"
        );
    }
}
