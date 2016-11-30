package com.theplatform.module.docker.client;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.DockerRequestException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.theplatform.module.docker.elastic.InstanceRegulator;
import com.theplatform.module.docker.elastic.InstanceRegulatorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NOT THREAD-SAFE.
 */
public class DockerContainerRegulatorClient implements InstanceRegulatorClient
{
    private static Logger logger = LoggerFactory.getLogger(InstanceRegulator.class);

    private ConcurrentLinkedQueue<String> names = new ConcurrentLinkedQueue<>();
    private DockerClient dockerClient;
    private String containerNamePrefix;
    private String imageName;
    private int secodsToWaitBeforeKill = 3;
    private int secodsToWaitBeforeKillAll = 10;
    private String configVolume;
    private String logLevel;
    private String heapSize;
    private String networkMode = "bridge";
    private List<String> commands;

    public void setNetworkMode(String networkMode)
    {
        this.networkMode = networkMode;
    }

    /**
     * FORMAT = "local:container"
     * ie.    = "/host/location/which/is/local:/now/inside/container
     * This example would take a directory
     * /host/location/which/is/local
     * and make it available inside a docker container at
     * /now/inside/container
     *
     * @param configVolume volume binding
     */
    public void setConfigVolume(String configVolume)
    {
        this.configVolume = configVolume;
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
        if (configVolume != null)
        {
            HostConfig.Builder hostConfigBuilder = HostConfig.builder();
            hostConfigBuilder.networkMode(networkMode);
            HostConfig hostConfig = hostConfigBuilder.binds(configVolume).build();
            containerBuilder = containerBuilder.hostConfig(hostConfig);
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

        ContainerCreation cc = null;
        try
        {
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

    private String getName(String nameSuffix)
    {
        return containerNamePrefix + "_" + nameSuffix;
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
        try (LogStream stream = dockerClient.logs(getName(nameSuffix), DockerClient.LogsParameter.STDOUT, DockerClient.LogsParameter.STDERR))
        {
            output = stream.readFully();
        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Error getting standard output from container [" + getName(nameSuffix) + "] ", e);
        }
        return output;
    }

    public void waitForInstance(String nameSuffix)
    {
        try
        {
            dockerClient.waitContainer(getName(nameSuffix));
        }
        catch (InterruptedException | DockerException e)
        {
            logger.error("Error waiting for container to finish [" + getName(nameSuffix) + "] ", e);
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
