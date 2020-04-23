package com.comcast.pop.handler.executor.impl.executor.kubernetes;

import com.comcast.pop.handler.executor.impl.exception.AgendaExecutorException;
import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactory;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class KubernetesOperationExecutorFactoryTest
{
    private KubernetesOperationExecutorFactory executorFactory;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private OperationWrapper mockOperationWrapper;
    private ExecutorContext mockExecutorContext;
    private PropertyRetriever mockPropertyRetriever;
    private KubeConfigFactory mockKubeConfigFactory;
    private PodFollowerFactory mockPodFollowerFactory;

    @BeforeMethod
    public void setup()
    {
        mockOperationWrapper = mock(OperationWrapper.class);
        mockExecutorContext = mock(ExecutorContext.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockKubeConfigFactory = mock(KubeConfigFactory.class);
        mockPodFollowerFactory = mock(PodFollowerFactory.class);

        doReturn(true).when(mockPropertyRetriever).getBoolean("useStaticRegistryClient", false);
        doReturn(mockPropertyRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        doReturn(new KubeConfig()).when(mockKubeConfigFactory).createKubeConfig();
        doReturn(new JsonHelper()).when(mockExecutorContext).getJsonHelper();

        executorFactory = new KubernetesOperationExecutorFactory(mockLaunchDataWrapper);
        executorFactory.setKubeConfigFactory(mockKubeConfigFactory);
        executorFactory.setPodFollowerFactory(mockPodFollowerFactory);
    }

    @DataProvider
    public Object[][] createOperationExecutorProvider()
    {
        return new Object[][]
            {
                {true},
                {false}
            };
    }

    @Test(dataProvider = "createOperationExecutorProvider")
    public void testCreateOperationExecutor(boolean hasPriorProgress)
    {
        Operation operation = new Operation();
        operation.setType("sample");
        doReturn(operation).when(mockOperationWrapper).getOperation();

        doReturn(hasPriorProgress
                 ? new OperationProgress()
                 : null )
            .when(mockOperationWrapper).getPriorExecutionOperationProgress();

        KubernetesOperationExecutor kubernetesExecutor = callCreateOperationExecutor(mockExecutorContext, mockOperationWrapper);
        Assert.assertEquals(kubernetesExecutor.getPodConfig().getEnvVars().containsKey(HandlerField.LAST_PROGRESS.name()), hasPriorProgress);
    }

    @Test(expectedExceptions = AgendaExecutorException.class, expectedExceptionsMessageRegExp = ".*Unknown operation type found.*")
    public void testCreateOperationExecutorUnknownOp()
    {
        Operation operation = new Operation();
        operation.setType("unkown");
        doReturn(operation).when(mockOperationWrapper).getOperation();
        callCreateOperationExecutor(mockExecutorContext, mockOperationWrapper);
    }

    private KubernetesOperationExecutor callCreateOperationExecutor(ExecutorContext executorContext, OperationWrapper operationWrapper)
    {
        BaseOperationExecutor executor = executorFactory.createOperationExecutor(executorContext, operationWrapper);
        Assert.assertEquals(executor.getClass(), KubernetesOperationExecutor.class);
        return (KubernetesOperationExecutor) executor;
    }
}
