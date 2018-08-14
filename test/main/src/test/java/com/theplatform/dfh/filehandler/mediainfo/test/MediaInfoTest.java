package com.theplatform.dfh.filehandler.mediainfo.test;


import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.api.MediaInfoHandlerInput;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.impl.executor.factory.DockerMediaInfoExecutorFactory;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.impl.processor.MediaInfoProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.filehandler.mediaassembler.api.MediaProperties;
import com.theplatform.test.dfh.filemanager.utils.FtpToWWWLocalFileRetriever;
import com.theplatform.test.dfh.filemanager.utils.LocalFileCreator;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class MediaInfoTest
{
    private final String tfmHost = "http://testtptfm01:8080";
    public static final String COOLEST_DOG_SMALL_UUID = "ed709ac1-6889-498d-b079-28bcc2c9a923";
    public static final String CC_608_UUID = "facd470f-0095-11e8-a69a-005056a1532f";

    public static final String DOCKER_IMAGE_FOR_MEDIAINFO = "docker-prod.repo.theplatform.com/mediainfo:1.0";

    @Test
    public void testMediaInfoHappyPath() throws Exception
    {
        LocalFileCreator localFileCreator = new LocalFileCreator(tfmHost);
        String absolutePath = FtpToWWWLocalFileRetriever.getDefaultLocalFilePathAndName();

        String sourceFileName = "someSource.mp4";
        String localFilePathAndName = absolutePath + sourceFileName;

        File sourceToCleanUp = null;
        try
        {

            sourceToCleanUp = localFileCreator.create(COOLEST_DOG_SMALL_UUID, localFilePathAndName);
            MediaProperties properties = dockerExecute(localFilePathAndName);
            assertThat(properties).isNotNull();
            assertThat(properties.getVideoTrackCount()).isEqualTo(1);
            assertThat(properties.getAudioTracks()[0].getLanguage()).isEqualTo("eng");
        }
        finally
        {

            if (sourceToCleanUp != null && sourceToCleanUp.exists())
            {
                sourceToCleanUp.delete();
            }
        }
    }

    @Test
    public void testCCEmbedded() throws Exception
    {
        LocalFileCreator localFileCreator = new LocalFileCreator(tfmHost);
        String absolutePath = FtpToWWWLocalFileRetriever.getDefaultLocalFilePathAndName();

        String sourceFileName = "someSource.mp4";
        String localFilePathAndName = absolutePath + sourceFileName;

        File sourceToCleanUp = null;
        try
        {
            sourceToCleanUp = localFileCreator.create(CC_608_UUID, localFilePathAndName);
            MediaProperties properties = dockerExecute(localFilePathAndName);
            assertThat(properties).isNotNull();
            assertThat(properties.getVideoTrackCount()).isEqualTo(1);
            assertThat(properties.getAudioTracks().length).isEqualTo(2);
            assertThat(properties.getAudioTracks()[0].getLanguage()).isEqualTo("eng");
            assertThat(properties.getAudioTracks()[1].getLanguage()).isEqualTo("spa");
            assertThat(properties.getVideoTracks().length).isEqualTo(1);
        }
        finally
        {

            if (sourceToCleanUp != null && sourceToCleanUp.exists())
            {
                sourceToCleanUp.delete();
            }
        }
    }

    private MediaProperties dockerExecute(String filePath)
    {
        DockerMediaInfoExecutorFactory mediaInfoExecutorFactory = new DockerMediaInfoExecutorFactory();

        MediaInfoHandlerInput mediaInfoHandlerInput = new MediaInfoHandlerInput();
        InputFileResource inputFileResource = new InputFileResource();
        inputFileResource.setUrl(filePath);
        mediaInfoHandlerInput.setInputs(new ArrayList<>());
        mediaInfoHandlerInput.getInputs().add(inputFileResource);

        JsonHelper mockJsonHelper = mock(JsonHelper.class);
        doReturn(mediaInfoHandlerInput).when(mockJsonHelper).getObjectFromString(any(), any());
        PropertyRetriever mockPropertyRetriever = mock(PropertyRetriever.class);
        doReturn(DOCKER_IMAGE_FOR_MEDIAINFO).when(mockPropertyRetriever).getField("dockerImageName");

        LaunchDataWrapper launchDataWrapper = new DefaultLaunchDataWrapper(mock(FieldRetriever.class), mock(FieldRetriever.class), mockPropertyRetriever);
        launchDataWrapper.setPropertyRetriever(mockPropertyRetriever);
        OperationContext operationContext = new OperationContext(mediaInfoExecutorFactory, new LogReporter());

        MediaInfoProcessor mediaInfoProcessor = new MediaInfoProcessor(launchDataWrapper, operationContext);
        mediaInfoProcessor.setJsonHelper(mockJsonHelper);
        return mediaInfoProcessor.execute();
    }
}