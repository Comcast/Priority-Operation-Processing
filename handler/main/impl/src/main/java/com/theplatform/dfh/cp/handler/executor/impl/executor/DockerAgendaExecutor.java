package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.module.docker.client.DockerContainerRegulatorClient;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Docker specific implementation of the mediainfo executor
 */
public class DockerAgendaExecutor extends AgendaExecutor
{
    protected final String dockerClientUrl = "unix:///var/run/docker.sock";
    private String dockerImageName;
    private DockerClientFactory dockerClientFactory;

    public DockerAgendaExecutor(String filePath)
    {
        super(filePath);
        this.dockerClientFactory = new DockerClientFactory();
    }

    public DockerAgendaExecutor setDockerImageName(String dockerImageName)
    {
        this.dockerImageName = dockerImageName;
        return this;
    }

    protected List<String> getProgressAndWait(DockerContainerRegulatorClient client, String suffix)
        throws Exception
    {
        long linesProcessed = 0L;
        LinkedList<String> lines = new LinkedList<>();
        long timeStarted = System.currentTimeMillis();
        long timeSpent = 0;
        // Loop while the container is running
        while (client.isInstanceRunning(suffix))
        {
            Thread.sleep(1000l);
            lines.addAll(client.getStandardOutput(suffix, linesProcessed));
            // Send the new lines to the output parser
            timeSpent = System.currentTimeMillis() - timeStarted;
            int linesSeen = lines.size();
            logger
                .debug("processed {} lines from mediaInfo, totalProcessed {} for container w/suffix {}",
                    linesSeen,
                    linesProcessed,
                    suffix );

            // Remember where we left off
            linesProcessed += linesSeen;
        }

        if (logger.isDebugEnabled())
        {
            String allLines = lines.stream().map(i -> i.toString()).collect(Collectors.joining(""));
            logger.debug(allLines.replace("\n", ""));
        }
        return lines;
    }

    @Override
    public List<String> execute()
    {
        List<String> mediaInfoCommands = Arrays.asList("--Output=XML", "-f", getFilePath());
        logger.info("Using docker client {}", dockerClientUrl);
        DockerContainerRegulatorClient client = new DockerContainerRegulatorClient();
        client.setContainerNamePrefix("MEDIA-INFO");
        client.setImageName(dockerImageName);
        client.setDockerClient(dockerClientFactory.createDockerClient(dockerClientUrl));

        // Set the volume mappings
        Set<String> volumeMappings = new HashSet<>();

        String srcFolder = Paths.get(getFilePath()).getParent().toString();

        volumeMappings.add(srcFolder + ":" + srcFolder);

        client.setVolumeMappings(volumeMappings);
        client.setCommands(mediaInfoCommands);

        // Create a new container name suffix
        String containerNameSuffix = generateContainerNameSuffix();

        try
        {
            // Start the container
            logger.info("Starting the container w/suffix {}.", containerNameSuffix);
            client.startInstance(containerNameSuffix);

            // Get progress until the container is finished
            logger.info("Getting progress until the container is finished.");
            return getProgressAndWait(client, containerNameSuffix);
        }
        catch (Exception e)
        {
            logger.error("Exception thrown while executing mediaInfo.", e);
            throw new AgendaExecutorException(e);
        }
        finally
        {
            //wait for container to completely exit and get error code
            int exitCode;

            try
            {
                exitCode = client.waitForInstance(containerNameSuffix);
            }
            catch (Exception e)
            {
                logger.info(e.getMessage());
                throw new AgendaExecutorException(e);
            }

            if (exitCode != 0)
            {
                logger.info("mediaInfo exited with ExitCode " + exitCode);
                String standardOutput = client.getStandardOutput(containerNameSuffix);
                logger.error(standardOutput);
                throw new AgendaExecutorException(standardOutput);
            }

            // Stop and remove the container
            logger.info("Stopping container w/suffix {}.", containerNameSuffix);
            client.stopInstance(containerNameSuffix);

            // Disconnect from docker
            logger.info("Disconnecting from Docker used for container w/suffix {}.", containerNameSuffix);
            client.close();
        }
    }

    public void setDockerClientFactory(DockerClientFactory dockerClientFactory)
    {
        this.dockerClientFactory = dockerClientFactory;
    }
}