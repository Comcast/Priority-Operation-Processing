package com.theplatform.module.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.theplatform.module.docker.client.DockerContainerRegulatorClient;
import com.theplatform.module.docker.elastic.InstanceRegulator;
import com.theplatform.module.docker.elastic.RegulatorFactory;
import com.theplatform.module.docker.elastic.RegulatorState;
import com.theplatform.module.docker.elastic.demand.HandlerDemandFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class InstanceRegulatorTest
{
    InstanceRegulator instanceRegulator;
    DockerClient dockerClient;

    @BeforeMethod
    public void setUp() throws Exception
    {
        dockerClient = mock(DockerClient.class);
        instanceRegulator = RegulatorFactory
            .getDockerRegulator(dockerClient, "fhc", "docker-lab.repo.theplatform.com/fhc:1.0.3");
    }

    DockerClient.ListContainersParam getAnyParam()
    {
        return any(DockerClient.ListContainersParam.class);
    }

    @DataProvider
    public Object[][] capacityDataProvider()
    {
        return new Object[][]
            {
                // min,
                //| max,
                //| |  curr,
                //| |  |   how to evaluate state
                //| |  |   |
                { 0, 0, 0, RegulatorState.justRight },
                { 0, 0, 1, RegulatorState.needLess },
                { 0, 1, 1, RegulatorState.justRight },
                { 0, 2, 1, RegulatorState.needMore },
                //
                { 1, 1, 1, RegulatorState.justRight },
                { 1, 1, 2, RegulatorState.needLess },
                { 1, 2, 0, RegulatorState.needMore },
                //
                { 1, 3, 3, RegulatorState.justRight },
                { 1, 3, 4, RegulatorState.needLess },
                { 1, 3, 1, RegulatorState.needMore },
            };
    }

    @Test(dataProvider = "capacityDataProvider")
    public void testCapacityCalcs(int minInstances, int maxInstances, int currInstanceCount, RegulatorState regulatorState) throws Exception
    {
        DockerContainerRegulatorClient client = mock(DockerContainerRegulatorClient.class);

        InstanceRegulator regulator = new InstanceRegulator();

        regulator.setMinimumInstances(minInstances);
        regulator.setMaximumInstances(maxInstances);

        if (regulatorState == RegulatorState.justRight)
        {
            Assert.assertFalse(regulator.isOverCapacity(currInstanceCount));
            Assert.assertTrue(regulator.isCarryingCapacityMet(currInstanceCount));
        }
        else if (regulatorState == RegulatorState.needLess)
        {
            Assert.assertTrue(regulator.isOverCapacity(currInstanceCount));
            Assert.assertTrue(regulator.isCarryingCapacityMet(currInstanceCount));
        }
        else
        {
            Assert.assertFalse(regulator.isOverCapacity(currInstanceCount));
            Assert.assertFalse(regulator.isCarryingCapacityMet(currInstanceCount));
        }
    }

    @Test
    public void testTooManyExistingInstances() throws Exception
    {
        List<Container> containers = new LinkedList<>();
        List<String> names = getNames(1, 2);
        containers.add(getContainer(names));
        containers.add(getContainer(names));
        when(dockerClient.listContainers(getAnyParam())).thenReturn(containers);

        instanceRegulator.setMinimumInstances(1);
        instanceRegulator.setMaximumInstances(1);
        instanceRegulator.regulate();
        verify(dockerClient, times(1)).listContainers(getAnyParam());
        verify(dockerClient, times(0)).startContainer(anyString());
        verify(dockerClient, times(1)).stopContainer(anyString(), anyInt());

        Assert.assertTrue(instanceRegulator.isComplete());
        verify(dockerClient, times(1)).listContainers(getAnyParam());
    }

    private List<String> getNames(int ... args)
    {
        LinkedList<String> names = new LinkedList<>();
        for(int i : args)
        {
            names.add("fhc_" + i);
        }

        return names;
    }

    private Container getContainer(List<String> names)
    {
        Container container = new Container()
        {
            @Override
            public List<String> names()
            {
                return names;
            }
        };


        return container;
    }

    @Test
    public void testRegulatorSeesZero() throws Exception
    {
        int currentHandlerCount = instanceRegulator.getCurrentInstanceCount();

        Assert.assertEquals(0, currentHandlerCount);
        verify(dockerClient).listContainers(getAnyParam());
    }

    @Test
    public void testRegulatorSeesNonZero() throws Exception
    {
        List<Container> containers = new LinkedList<>();
        containers.add(getContainer(null));
        when(dockerClient.listContainers(getAnyParam())).thenReturn(containers);

        int currentHandlerCount = instanceRegulator.getCurrentInstanceCount();

        Assert.assertEquals(1, currentHandlerCount);
        verify(dockerClient).listContainers(getAnyParam());
    }

    @Test
    public void testRegulatorAttemptsToIncreaseFromZero() throws Exception
    {
        ContainerCreation cc = mock(ContainerCreation.class);
        when(dockerClient.createContainer(any(ContainerConfig.class), anyString())).thenReturn(cc);

        instanceRegulator.setMinimumInstances(1);
        instanceRegulator.setMaximumInstances(1);
        instanceRegulator.regulate();

        verify(dockerClient).listContainers(getAnyParam());
        verify(dockerClient).startContainer(anyString());
    }

    @Test
    public void testRegulatorMeetsLowMaximumAndStops() throws Exception
    {
        int minimumAllowableInstances = 1;
        int maximumAllowableInstances = 1;
        instanceRegulator.setMinimumInstances(minimumAllowableInstances);
        instanceRegulator.setMaximumInstances(maximumAllowableInstances);

        HandlerDemandFactory demandFactory = mock(HandlerDemandFactory.class);
        instanceRegulator.setDemandFactory(demandFactory);
        when(demandFactory.isDemandPresent()).thenReturn(true);

        ContainerCreation cc = mock(ContainerCreation.class);
        when(dockerClient.createContainer(any(ContainerConfig.class), anyString())).thenReturn(cc);

        // FIRST CALL TO INSPECT INSTANCES AND ADJUST
        instanceRegulator.regulate();

        // VERIFY FOR FIRST CALL
        verify(dockerClient, times(1)).listContainers(getAnyParam());
        verify(dockerClient).startContainer(anyString());

        // SETUP FOR 2ND CALL
        List<Container> containers = new LinkedList<>();
        containers.add(getContainer(null));
        when(dockerClient.listContainers(getAnyParam())).thenReturn(containers);

        // SECOND CALL TO INSPECT INSTANCES AND ADJUST
        instanceRegulator.regulate();

        // VERIFY THE SECOND CALL
        verify(dockerClient, times(2)).listContainers(getAnyParam());
        verify(dockerClient, times(1)).createContainer(any(ContainerConfig.class), anyString());
    }

    @Test
    public void testRegulatorMeetsHigherMaximumAndStops() throws Exception
    {
        int minimumAllowableInstances = 1;
        int maximumAllowableInstances = 3;
        instanceRegulator.setMinimumInstances(minimumAllowableInstances);
        instanceRegulator.setMaximumInstances(maximumAllowableInstances);

        // SETUP INFINITE DEMAND FOR HANDLERS
        HandlerDemandFactory demandFactory = mock(HandlerDemandFactory.class);
        instanceRegulator.setDemandFactory(demandFactory);
        when(demandFactory.isDemandPresent()).thenReturn(true);

        // SETUP CONTAINER BEHAVIOR WHENEVER CALLED
        ContainerCreation cc = mock(ContainerCreation.class);
        when(dockerClient.createContainer(any(ContainerConfig.class), anyString())).thenReturn(cc);

        // FIRST CALL TO INSPECT INSTANCES AND ADJUST
        instanceRegulator.regulate();

        // VERIFY FOR FIRST CALL
        verify(dockerClient, times(1)).listContainers(getAnyParam());
        verify(dockerClient, times(1)).createContainer(any(ContainerConfig.class), anyString());
        verify(dockerClient, times(1)).startContainer(anyString());

        // SETUP FOR 2ND CALL
        List<Container> containers = new LinkedList<>();
        containers.add(getContainer(null));
        when(dockerClient.listContainers(getAnyParam())).thenReturn(containers);

        // SECOND CALL TO INSPECT INSTANCES AND ADJUST
        instanceRegulator.regulate();
        verify(dockerClient, times(2)).listContainers(getAnyParam());
        verify(dockerClient, times(2)).createContainer(any(ContainerConfig.class), anyString());
        verify(dockerClient, times(2)).startContainer(anyString());

        // add container to mock
        containers.add(getContainer(null));

        instanceRegulator.regulate();

        verify(dockerClient, times(3)).listContainers(getAnyParam());
        verify(dockerClient, times(3)).createContainer(any(ContainerConfig.class), anyString());
        verify(dockerClient, times(3)).startContainer(anyString());
    }

    @Test
    public void testGetCurrentHandlerCountCallsDockerDaemon() throws Exception
    {
        List<Container> containers = new LinkedList<>();
        when(dockerClient.listContainers()).thenReturn(containers);
        int currentCountOfHandlers = instanceRegulator.getCurrentInstanceCount();
        Assert.assertEquals(currentCountOfHandlers, 0);
        verify(dockerClient).listContainers(getAnyParam());
    }
}
