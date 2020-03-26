package com.theplatform.module.docker.client;

import com.google.common.base.Charsets;
import com.spotify.docker.client.*;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.*;
import com.theplatform.module.docker.elastic.InstanceRegulatorClient;
import com.theplatform.module.docker.exception.DfhDockerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NOT THREAD-SAFE.
 */
public class DockerContainerRegulatorClient implements InstanceRegulatorClient
{
    private static Logger logger = LoggerFactory.getLogger(DockerContainerRegulatorClient.class);

    private ConcurrentLinkedQueue<String> names = new ConcurrentLinkedQueue<>();
    private DockerClient dockerClient;
    private String containerNamePrefix;
    private String imageName;
    private int secodsToWaitBeforeKill = 3;
    private int secodsToWaitBeforeKillAll = 10;
    private Set<String> volumeMappings;
    private String logLevel;
    private String heapSize;
    private String networkMode = "bridge";
    private List<String> commands;
    private boolean isEchoTest = false;
    private String entryPoint = null;
    private boolean isSkipPull = false;


    public boolean isSkipPull()
    {
        return isSkipPull;
    }

    public void setSkipPull(boolean skipPull)
    {
        isSkipPull = skipPull;
    }

    public void setEchoTest(boolean echoTest)
    {
        isEchoTest = echoTest;
    }

    public boolean isEchoTest()
    {
        return isEchoTest;
    }

    public String getEntryPoint()
    {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint)
    {
        this.entryPoint = entryPoint;
    }

    public void setNetworkMode(String networkMode)
    {
        this.networkMode = networkMode;
    }

    /**
     * Each String element of the list is:
     * FORMAT = "local:container"
     * ie.    = "/host/location/which/is/local:/now/inside/container
     * This example would take a directory
     * /host/location/which/is/local
     * and make it available inside a docker container at
     * /now/inside/container
     *
     * @param volumeMappings volume bindings
     */
    public void setVolumeMappings(Set<String> volumeMappings)
    {
        this.volumeMappings = volumeMappings;
    }

    /**
     * FORMAT = "1g" or "256m"
     *
     * @param heapSize "1g"
     */
    public void setHeapSize(String heapSize)
    {
        this.heapSize = heapSize;
    }

    /**
     * FORMAT "WARN" "DEBUG" and so on.
     *
     * @param logLevel level
     */
    public void setLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
    }

    public void setSecodsToWaitBeforeKill(int secodsToWaitBeforeKill)
    {
        this.secodsToWaitBeforeKill = secodsToWaitBeforeKill;
    }

    public void setSecodsToWaitBeforeKillAll(int secodsToWaitBeforeKillAll)
    {
        this.secodsToWaitBeforeKillAll = secodsToWaitBeforeKillAll;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public void setContainerNamePrefix(String containerNamePrefix)
    {
        this.containerNamePrefix = containerNamePrefix;
    }

    public void setDockerClient(DockerClient dockerClient)
    {
        this.dockerClient = dockerClient;
    }

    public void setCommands(List<String> commands)
    {
        this.commands = commands;
    }

    @Override
    public boolean startInstance(String nameSuffix)
    {
        logger.debug("Starting new instance");

        ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        if (volumeMappings != null && volumeMappings.size() > 0)
        {
            HostConfig.Builder hostConfigBuilder = HostConfig.builder();
            hostConfigBuilder.networkMode(networkMode);
            ArrayList<String> volumes = new ArrayList<>(volumeMappings);
            HostConfig hostConfig = hostConfigBuilder.binds(volumes).build();
            containerBuilder = containerBuilder.hostConfig(hostConfig);
        }

        if(isEchoTest)
        {
            containerBuilder.entrypoint("echo");
        }

        if(entryPoint != null && entryPoint.length() > 0)
        {
            containerBuilder.entrypoint(entryPoint);
        }

        List<String> envs = new LinkedList<>();
        if (logLevel != null)
        {
            envs.add("LOG_LEVEL=" + logLevel);
        }
        if (heapSize != null)
        {
            envs.add("JAVA_HEAP=" + heapSize);
        }
        if (envs.size() > 0)
        {
            containerBuilder = containerBuilder.env(envs);
        }

        if (commands != null && commands.size() > 0)
        {
            containerBuilder = containerBuilder.cmd(commands);
        }

        String containerName = getName(nameSuffix);
        ContainerConfig containerConfig = containerBuilder.image(imageName).hostname(containerName).build();

        logger.debug("ContainerConfig {} ", containerConfig.toString());

        ContainerCreation cc = null;
        try
        {
            // Image pull won't happen automatically, we have to do it explicitly
            // Version should be in the imageName
            if(!isSkipPull)
            {
                pull(imageName);
            }

            // Now that the correct version of the image is available, we should be able to create a container
            cc = dockerClient.createContainer(containerConfig, containerName);
            dockerClient.startContainer(cc.id());
            return true;
        }
        catch (DockerException | InterruptedException e)
        {
            if (e instanceof DockerRequestException)
            {
                DockerRequestException requestException = (DockerRequestException) e;
                // 409 is apparently what you get when the name already exists.
                if (requestException.status() == 409)
                {
                    return false;
                }
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void pull(String imageName)
    {
        try
        {
            dockerClient.pull(imageName);
        }
        catch (DockerException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getName(String nameSuffix)
    {
        return containerNamePrefix + nameSuffix;
    }

    @Override
    public int getCurrentInstanceCount()
    {
        return getCurrentContainers().size();
    }

    public List<Container> getCurrentContainers()
    {
        DockerClient.ListContainersParam param = new DockerClient.ListContainersParam("name", containerNamePrefix);
        try
        {
            List<Container> containers = dockerClient.listContainers(param);
            if (containers.size() != names.size() && names.size() > 0)
            {
                names.clear();
            }
            containers.forEach(
                c -> {
                    if (c.names() != null)
                    {
                        String name = c.names().get(0);
                        logger.debug("Found running container with name:" + name);
                        names.add(name);
                    }
                }
            );
            return containers;
        }
        catch (DockerException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopInstance(String nameSuffix)
    {
        try
        {
            if (nameSuffix == null && names.size() > 0)
            {
                String name = names.poll();
                if (name != null)
                {
                    dockerClient.stopContainer(name, secodsToWaitBeforeKill);
                    dockerClient.removeContainer(name, true);
                }
            }
            else if (nameSuffix != null)
            {
                dockerClient.stopContainer(getName(nameSuffix), secodsToWaitBeforeKill);
                dockerClient.removeContainer(getName(nameSuffix));
            }
        }
        catch (DockerException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getStandardOutput(String nameSuffix)
    {
        String output = null;
        try (LogStream logStream = dockerClient.logs(getName(nameSuffix), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr()))
        {
            output = logStream.readFully();
        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Error getting standard output from container [" + getName(nameSuffix) + "] ", e);
        }
        return output;
    }

    public List<String> getStandardOutput(String nameSuffix, long linesToSkip)
    {
        List<String> lines = new ArrayList<>();

        try (LogStream logStream = dockerClient.logs(getName(nameSuffix), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr()))
        {
            while (logStream.hasNext())
            {
                if (linesToSkip > 0)
                {
                    logStream.next();
                    linesToSkip--;
                }
                else
                {
                    lines.add(Charsets.UTF_8.decode(((LogMessage)logStream.next()).content()).toString());
                }
            }
        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Error getting standard output from container [" + getName(nameSuffix) + "] ", e);
        }

        return lines;
    }

    public int waitForInstance(String nameSuffix)
    {
        try
        {
            ContainerExit containerExit = dockerClient.waitContainer(getName(nameSuffix));
            if (containerExit != null)
            {
                logger.info("Container finished with status code: " + containerExit.statusCode());
                return containerExit.statusCode();
            }
            else {
                throw new RuntimeException("Container finished without a status code.");
            }


        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Error waiting for container to finish [" + getName(nameSuffix) + "] ", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isInstanceRunning(String nameSuffix) throws DfhDockerException
    {
        try
        {
            return dockerClient.inspectContainer(getName(nameSuffix)).state().running();
        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Exception while inspecting container for running status [" + getName(nameSuffix) + "] ", e);
            throw new DfhDockerException(e);
        }
    }

    @Override
    public boolean checkInstance(String nameSuffix)
    {
        DockerClient.ListContainersParam listContainersParam = new DockerClient.ListContainersParam(
            "name", getName(nameSuffix));
        try
        {
            List<Container> containers = dockerClient.listContainers(listContainersParam);
            return containers.size() > 0;
        }
        catch (DockerException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopAll()
    {
        getCurrentContainers().forEach(c ->
        {
            try
            {
                dockerClient.stopContainer(c.id(), secodsToWaitBeforeKillAll);
                dockerClient.removeContainer(c.id(), true);
            }
            catch (DockerException | InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void close()
    {
        dockerClient.close();
    }
}
