package com.comcast.fission.handler.executor.impl.processor.operation.generator;

import com.comcast.fission.handler.executor.impl.context.ExecutorContext;
import com.comcast.fission.handler.executor.impl.processor.OperationWrapper;
import com.comcast.fission.handler.executor.impl.processor.OperationWrapperFactory;
import com.comcast.fission.handler.executor.impl.processor.parallel.OperationConductor;
import com.comcast.fission.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GeneratedOperationsModifierTest
{
    final String PARAM_KEY = "theKey";

    private ParamsMap testParams = new ParamsMap();
    private List<Operation> testOps = IntStream.range(0, 10).mapToObj(i -> new Operation()).collect(Collectors.toList());

    private JsonHelper jsonHelper = new JsonHelper();
    private GeneratedOperationsModifier modifier;
    private ExecutorContext mockExecutorContext;
    private OperationConductor mockOperationConductor;
    private Operation operation;
    private OperationWrapper mockOperationWrapper;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private PropertyRetriever mockPropertyRetriever;
    private OperationWrapperFactory mockOperationWrapperFactory;

    @BeforeMethod
    public void setup()
    {
        // TODO: care about the value?
        testParams.put(PARAM_KEY, PARAM_KEY);

        operation = new Operation();
        mockOperationWrapper = mock(OperationWrapper.class);
        doReturn(operation).when(mockOperationWrapper).getOperation();
        mockOperationConductor = mock(OperationConductor.class);
        mockExecutorContext = mock(ExecutorContext.class);
        doReturn(jsonHelper).when(mockExecutorContext).getJsonHelper();
        mockOperationWrapperFactory = mock(OperationWrapperFactory.class);
        // TODO: may want to return another wrapper...
        doReturn(mockOperationWrapper).when(mockOperationWrapperFactory).create(any());
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        doReturn(mockLaunchDataWrapper).when(mockExecutorContext).getLaunchDataWrapper();
        doReturn(mockPropertyRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        doReturn(mock(AgendaProgressReporter.class)).when(mockExecutorContext).getAgendaProgressReporter();
        modifier = new GeneratedOperationsModifier(mockExecutorContext);
        modifier.setOperationWrapperFactory(mockOperationWrapperFactory);
    }

    @Test
    public void testNonOperationGenerator()
    {
        modifier.modify(mockExecutorContext, mockOperationWrapper, mockOperationConductor);
        verify(mockExecutorContext, times(0)).getJsonHelper();
    }

    @Test
    public void testOperationGeneratorWithOutParams()
    {
        List<Operation> generatedOps = IntStream.range(0, 10).mapToObj(i -> new Operation()).collect(Collectors.toList());
        TestGeneratedOutputObject generatedOutputObject = new TestGeneratedOutputObject(generatedOps, null);
        doReturn(jsonHelper.getJSONString(generatedOutputObject)).when(mockOperationWrapper).getOutputPayload();
        setupOperationOutputDefinition(TestGeneratedOutputObject.OPERATIONS_PATH, null);
        modifier.modify(mockExecutorContext, mockOperationWrapper, mockOperationConductor);
    }

    @Test
    public void testOperationGeneratorWithBadParamsPath()
    {
        List<Operation> generatedOps = IntStream.range(0, 10).mapToObj(i -> new Operation()).collect(Collectors.toList());
        TestGeneratedOutputObject generatedOutputObject = new TestGeneratedOutputObject(generatedOps, null);
        doReturn(jsonHelper.getJSONString(generatedOutputObject)).when(mockOperationWrapper).getOutputPayload();
        setupOperationOutputDefinition(TestGeneratedOutputObject.OPERATIONS_PATH, "/badpath");
        modifier.modify(mockExecutorContext, mockOperationWrapper, mockOperationConductor);
    }

    @DataProvider
    public Object[][] successfulInputsProvider()
    {
        return new Object[][]
            {
                { testOps, testParams, TestGeneratedOutputObject.OPERATIONS_PATH, TestGeneratedOutputObject.PARAMS_PATH },
                { testOps, null, TestGeneratedOutputObject.OPERATIONS_PATH, null },
                { testOps, null, TestGeneratedOutputObject.OPERATIONS_PATH, "/badPath" },
            };
    }

    @Test(dataProvider = "successfulInputsProvider")
    public void runSuccessfulTest(List<Operation> operations, ParamsMap params, String opPath, String paramsPath)
    {
        TestGeneratedOutputObject generatedOutputObject = new TestGeneratedOutputObject(operations, params);
        doReturn(jsonHelper.getJSONString(generatedOutputObject)).when(mockOperationWrapper).getOutputPayload();
        setupOperationOutputDefinition(opPath, paramsPath);
        modifier.modify(mockExecutorContext, mockOperationWrapper, mockOperationConductor);
    }

    private void setupOperationOutputDefinition(String generatedOpsPath, String generatedParamsPath)
    {
        ParamsMap params = new ParamsMap();
        params.put(GeneratedOperationsModifier.OPERATION_GENERATOR, new OperationGeneratorOutputDefinition(generatedOpsPath, generatedParamsPath));
        operation.setParams(params);
    }
}
