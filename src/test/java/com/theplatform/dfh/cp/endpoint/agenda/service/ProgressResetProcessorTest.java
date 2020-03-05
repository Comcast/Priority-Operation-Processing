package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaParameter;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProgressResetProcessorTest
{
    private static final String VALID_OP_NAME = "VALID";
    private ProgressResetProcessor progressResetProcessor;

    @BeforeMethod
    public void setup()
    {
        progressResetProcessor = new ProgressResetProcessor();
    }

    @Test
    public void getSpecifiedOperationsToReset()
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(VALID_OP_NAME);
        agendaProgress.setOperationProgress(new OperationProgress[] {operationProgress});

        String param = ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue(VALID_OP_NAME);

        Set<String> opsToReset = progressResetProcessor.getSpecifiedOperationsToReset(
            ReigniteAgendaParameter.getParametersMap(Collections.singletonList(param)),
            agendaProgress);

        Assert.assertEquals(opsToReset.size(), 1);
        Assert.assertTrue(opsToReset.contains(StringUtils.lowerCase(OperationProgress.generateId(agendaProgress.getId(), VALID_OP_NAME))));
    }

    @DataProvider
    public Object[][] invalidSpecifiedOperationsToReset()
    {
        return new Object[][]
        {
            {null},
            {Arrays.asList("")},
            {Arrays.asList("invalid")},
            {Arrays.asList("invalid1","invalid2")},
        };
    }

    @Test(dataProvider = "invalidSpecifiedOperationsToReset", expectedExceptions = ValidationException.class)
    public void getSpecifiedOperationsToResetInvalid(List<String> opsToReset)
    {
        String param = opsToReset == null
                       ? ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName()
                       : ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue(opsToReset);
        AgendaProgress agendaProgress = new AgendaProgress();
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(VALID_OP_NAME);
        agendaProgress.setOperationProgress(new OperationProgress[] {operationProgress});

        progressResetProcessor.getSpecifiedOperationsToReset(
            ReigniteAgendaParameter.getParametersMap(Collections.singletonList(param)),
            agendaProgress);
    }
}
