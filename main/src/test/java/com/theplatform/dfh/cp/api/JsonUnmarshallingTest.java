package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.output.Video;
import com.theplatform.dfh.cp.api.source.Sources;
import com.theplatform.dfh.cp.api.source.Source;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class JsonUnmarshallingTest
{

    @Test
    public void testJsonToSources() throws Exception
    {
        Job job = (Job)JsonUtil.toObject(getStringFromResourceFile("Job.json"), Job.class);
        Assert.assertNotNull(job);
        Sources sources = job.getSources();
        Assert.assertNotNull(sources);
        Assert.assertNotNull(sources.getVideo());
        verifyVideo(sources.getVideo().get(0), "/mount/path/filename.mov", "myid", "mypassword");
        verifyText(sources.getText().get(0), "/mount/path/filename-en.srt", "myid", "mypassword");
        verifyText(sources.getText().get(1), "/mount/path/filename-es.srt", "myid", "mypassword");
        Assert.assertNotNull(job.getSourceStreams());
        Assert.assertNotNull(job.getSourceStreams().getVideo());
        Assert.assertNotNull(job.getOutputStreams());
        Assert.assertNotNull(job.getOutputStreams().getVideo());
        Assert.assertNotNull(job.getOutputStreams().getAudio());
        verifyVideo(job.getOutputStreams().getVideo().get(0),  1920, 1020, 8000000L);
        verifyVideo(job.getOutputStreams().getVideo().get(1),  1280, 720, 2400000L);
    }

    private void verifyVideo(Source source, String expectedURL, String expectedUsername, String expectedPassword)
    {
        verifySource(source, expectedURL, expectedUsername, expectedPassword);
    }
    private void verifyText(Source source, String expectedURL, String expectedUsername, String expectedPassword)
    {
        verifySource(source, expectedURL, expectedUsername, expectedPassword);
    }
    private void verifyVideo(Video dataObject, Integer width, Integer height, Long bitrate)
    {
        Assert.assertNotNull(dataObject);
        Assert.assertEquals(dataObject.getBitrate(), bitrate);
        Assert.assertEquals(dataObject.getWidth(), width);
        Assert.assertEquals(dataObject.getHeight(), height);
    }
    private void verifySource(Source source, String expectedURL, String expectedUsername, String expectedPassword)
    {
       Assert.assertNotNull(source);
       Assert.assertEquals(source.getUrl(), expectedURL);
       Assert.assertEquals(source.getUsername(), expectedUsername);
       Assert.assertEquals(source.getPassword(), expectedPassword);
    }
    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
                this.getClass().getResource(file),
                "UTF-8"
        );
    }
}
