package com.comcast.fission.handler.executor.impl.processor;

import com.comcast.fission.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
        operationWrapper.isReady(mockExecutorContext, new HashSet<>());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "All dependencies have been completed but there are still pending references.*")
    public void testIsReadyMissingReferences()
    {
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.addMissingReference("missing");
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
        Assert.assertFalse(operationWrapper.isReady(mockExecutorContext, new HashSet<>()));
        Assert.assertNull(operationWrapper.getInputPayload());
    }

    @Test
    public void testIsReady()
    {
        final String EXPECTED_RESULT = "expected";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        referenceReplacementResult.setResult(EXPECTED_RESULT);
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
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
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
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
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
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
        doReturn(referenceReplacementResult).when(mockJsonContext).processReferences(any(), anyList());
        doReturn(dependencySet).when(mockJsonContextReferenceParser).getOperationNames(any());
        operationWrapper.init(mockExecutorContext, mockJsonContextReferenceParser);

        Assert.assertTrue(operationWrapper.getDependencies().contains(DEPENDENCY));
    }

    @DataProvider
    public Object[][] declaredDependenciesProvider()
    {
        return new Object[][]
            {
                {new String[] {"a"}, new String[]{}},
                {new String[] {}, new String[]{}},
                {new String[] {"a", "b", "c"}, new String[]{}},
                {new String[] {"a", "b", "c"}, new String[]{"e", "f", "g"}}
            };
    }

    @Test(dataProvider = "declaredDependenciesProvider")
    public void testDeclaredDependencies(String[] dependsOnItems, String[] missingReferences)
    {
        Set<String> missingReferenceSet = new HashSet<>(Arrays.asList(missingReferences));
        doReturn(missingReferenceSet).when(mockJsonContextReferenceParser).getOperationNames(any());

        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(GeneralParamKey.dependsOn.getKey(), String.join(",", dependsOnItems));
        doReturn(paramsMap).when(mockOperation).getParams();
        operationWrapper.updateDependencies(missingReferenceSet, mockJsonContextReferenceParser);
        Set<String> dependencies = operationWrapper.getDependencies();
        Assert.assertTrue(dependencies.containsAll(Arrays.asList(dependsOnItems)));
        Assert.assertTrue(dependencies.containsAll(Arrays.asList(missingReferences)));
        Assert.assertEquals(dependencies.size(), dependsOnItems.length + missingReferences.length);
    }
}
