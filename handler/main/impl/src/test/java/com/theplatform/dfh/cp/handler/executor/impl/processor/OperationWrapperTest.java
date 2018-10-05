package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class OperationWrapperTest
{
    private OperationWrapper operationWrapper;
    private ExecutorContext mockExecutorContext;
    private JsonContext mockJsonContext;
    private Operation mockOperation;
    private JsonContextReferenceParser mockJsonContextReferenceParser;

    @BeforeMethod
    public void setup()
    {
        mockExecutorContext = mock(ExecutorContext.class);
        mockOperation = mock(Operation.class);
        mockJsonContextReferenceParser = mock(JsonContextReferenceParser.class);
        mockJsonContext = mock(JsonContext.class);
        doReturn(mockJsonContext).when(mockExecutorContext).getJsonContext();
        operationWrapper = new OperationWrapper(mockOperation);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testIsReadyInvalidReferences()
    {
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.addInvalidReference("invalid");
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        operationWrapper.isReady(mockExecutorContext, new HashSet<>());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "All dependencies have been completed but there are still pending references.*")
    public void testIsReadyMissingReferences()
    {
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.addMissingReference("missing");
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        Assert.assertFalse(operationWrapper.isReady(mockExecutorContext, new HashSet<>()));
        Assert.assertNull(operationWrapper.getInputPayload());
    }

    @Test
    public void testIsReady()
    {
        final String EXPECTED_RESULT = "expected";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.setResult(EXPECTED_RESULT);
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        Assert.assertTrue(operationWrapper.isReady(mockExecutorContext, new HashSet<>()));
        Assert.assertEquals(operationWrapper.getInputPayload(), EXPECTED_RESULT);
    }

    @Test
    public void testIsReadyWithIncompleteDependencies()
    {
        Set<String> dependencies = IntStream.range(0, 5).mapToObj(Integer::toString).collect(Collectors.toSet());
        operationWrapper.setDependencies(dependencies);

        final String EXPECTED_RESULT = "expected";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.setResult(EXPECTED_RESULT);
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        Assert.assertFalse(operationWrapper.isReady(mockExecutorContext, new HashSet<>()));
    }

    @Test
    public void testIsReadyWithCompleteDependencies()
    {
        Set<String> dependencies = IntStream.range(0, 5).mapToObj(Integer::toString).collect(Collectors.toSet());
        operationWrapper.setDependencies(dependencies);

        final String EXPECTED_RESULT = "expected";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.setResult(EXPECTED_RESULT);
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        Assert.assertTrue(operationWrapper.isReady(mockExecutorContext, dependencies));
        Assert.assertEquals(operationWrapper.getInputPayload(), EXPECTED_RESULT);
    }

    @Test
    public void testInitWithReferenceDependencies()
    {
        final String DEPENDENCY = "missing";
        Set<String> dependencySet = new HashSet<>();
        dependencySet.add(DEPENDENCY);
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.addMissingReference(DEPENDENCY);
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any());
        doReturn(dependencySet).when(mockJsonContextReferenceParser).getOperationNames(any());
        operationWrapper.init(mockExecutorContext, mockJsonContextReferenceParser);

        Assert.assertTrue(operationWrapper.getDependencies().contains(DEPENDENCY));
    }
}
