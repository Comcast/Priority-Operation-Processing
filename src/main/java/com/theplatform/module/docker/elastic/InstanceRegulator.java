package com.theplatform.module.docker.elastic;

import com.theplatform.module.docker.DockerContainerRegulatorClient;
import com.theplatform.module.docker.elastic.demand.HandlerDemandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THREAD SAFETY: None! All methods are synchronized for a good reason.
 */
public class InstanceRegulator
{
    private static Logger logger = LoggerFactory.getLogger(InstanceRegulator.class);

    private InstanceRegulatorClient instanceRegulatorClient;
    private int minimumInstances;
    private int maximumInstances;

    private HandlerDemandFactory demandFactory;
    private String idleInstance;
    private RegulatorState regulatorState = RegulatorState.justRight;

    synchronized public void setInstanceRegulatorClient(
        DockerContainerRegulatorClient instanceRegulatorClient)
    {
        this.instanceRegulatorClient = instanceRegulatorClient;
    }

    synchronized public void setMinimumInstances(int minimumInstances)
    {
        this.minimumInstances = minimumInstances;
    }

    synchronized public void setMaximumInstances(int maximumInstances)
    {
        this.maximumInstances = maximumInstances;
        if (maximumInstances < minimumInstances)
        {
            throw new IllegalArgumentException("max < min");
        }
    }

    synchronized public void regulate()
    {
        int count = instanceRegulatorClient.getCurrentInstanceCount();
        logger.debug("Starting state: " + regulatorState.name());
        if (count < minimumInstances)
        {
            regulatorState = RegulatorState.needMore;
            logger.debug("Need more to satisfy minumum");
        }
        else if (!isCarryingCapacityMet(count))
        {
            regulatorState = RegulatorState.needMore;
            logger.debug("Need more to satisfy demand");
        }
        else if (isOverCapacity(count))
        {
            regulatorState = RegulatorState.needLess;
            logger.debug("Need less to satisfy limit");
        }

        switch (regulatorState)
        {
            case justRight:
                logger.debug("Resting, no changes made to instance count.");
                break;
            case needLess:
                logger.debug("Lets reduce the count.");
                instanceRegulatorClient.stopInstance(getIdleInstance());
                break;
            case needMore:
                logger.debug("Lets add more to the count.");
                instanceRegulatorClient.startInstance((new Integer(count + 1)).toString());
                break;
        }
        regulatorState = RegulatorState.justRight;
        logger.debug("Ending state: " + regulatorState.name());
    }

    synchronized public boolean isOverCapacity(int count)
    {
        return count > maximumInstances;
    }

    synchronized public boolean isCarryingCapacityMet(int count)
    {
        if (demandFactory == null)
        {
            return count >= maximumInstances;
        }
        else
        {
            return !(demandFactory.isDemandPresent() && count < maximumInstances);
        }
    }

    synchronized public void setDemandFactory(HandlerDemandFactory demandFactory)
    {
        this.demandFactory = demandFactory;
    }

    synchronized public int getCurrentInstanceCount()
    {
        return instanceRegulatorClient.getCurrentInstanceCount();
    }

    synchronized public boolean isComplete()
    {
        return regulatorState == RegulatorState.justRight;
    }

    synchronized public String getIdleInstance()
    {
        return idleInstance;
    }

    synchronized public void setIdleInstance(String idleInstance)
    {
        this.idleInstance = idleInstance;
    }

    synchronized public InstanceRegulatorClient getInstanceRegulatorClient()
    {
        return instanceRegulatorClient;
    }

    synchronized public void killAll()
    {
        instanceRegulatorClient.stopAll();
    }

    synchronized public void close()
    {
        instanceRegulatorClient.close();
    }
}
