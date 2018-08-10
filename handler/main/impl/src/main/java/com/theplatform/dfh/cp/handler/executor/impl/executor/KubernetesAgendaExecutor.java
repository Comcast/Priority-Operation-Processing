package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.filehandler.k8.ImageExecutionDetails;
import com.theplatform.dfh.filehandler.k8.LastPhase;
import com.theplatform.dfh.filehandler.k8.PodFollower;
import com.theplatform.dfh.filehandler.k8.PodPushClient;
import com.theplatform.dfh.filehandler.k8.logging.LogLineObserver;

import java.util.*;
import java.util.function.Consumer;

public class KubernetesAgendaExecutor extends AgendaExecutor
{
    private String dockerImageName;
    protected PodFollower<PodPushClient> follower;
    protected PodPushClient client;

    public KubernetesAgendaExecutor(String filePath)
    {
        super(filePath);
    }

    public KubernetesAgendaExecutor setDockerImageName(String dockerImageName)
    {
        this.dockerImageName = dockerImageName;
        return this;
    }

    private Consumer<String> getLineConsumer(final List<String> linesForProcessing)
    {
        return new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                linesForProcessing.add(s);
            }
        };
    }

    private Consumer<String> getConsumer(final StringBuilder stdoutCapture)
    {
        return new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                stdoutCapture.append(s).append("\n");
            }
        };
    }

    public void setFollower(PodFollower<PodPushClient> follower)
    {
        this.follower = follower;
    }

    public void setClient(PodPushClient client)
    {
        this.client = client;
    }

    @Override
    public List<String> execute()
    {
        String[] mediaInfoCommands = new String[] { "--Output=XML", "-f", getFilePath() };
        logger.info("Using kube w/image {}", dockerImageName);

        ImageExecutionDetails imageExecutionDetails = new ImageExecutionDetails().setImageName(dockerImageName)
            .setArguments(mediaInfoCommands).setNamePrefix(CONTAINER_NAME_PREFIX).setEndOfLogIdentifier(
                MEDIAINFO_END_OF_XML);
        logger.debug("Executing mediaInfo w/details {}", imageExecutionDetails);
        String podName = imageExecutionDetails.getName();
        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(imageExecutionDetails);

        logger.info("Getting progress until the pod {} is finished.", podName);
        StringBuilder allStdout = new StringBuilder();
        List<String> linesForProcessing = new LinkedList<>();
        logLineObserver.addConsumer(getConsumer(allStdout));
        logLineObserver.addConsumer(getLineConsumer(linesForProcessing));

        LastPhase lastPodPhase = null;
        try
        {
            logger.info("Starting the pod with name {}", podName);

            lastPodPhase = follower.startAndFollowPod(client, imageExecutionDetails, logLineObserver);

            logger.info("MediaInfo completed with pod status {}", lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    logger.error("MediaInfo failed to produce metadata, output was : {}", allStdout);
                    throw new AgendaExecutorException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("MediaInfo produced: {}", allStdout.toString());
            }
        }
        catch (Exception e)
        {
            String allStringMetadata = allStdout.toString();
            logger.error("Exception caught {}", allStringMetadata, e);
            throw new AgendaExecutorException(allStringMetadata, e);
        }
        return linesForProcessing;
    }
}