package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AgendaProgressBuilderTest
{
    private AgendaProgressBuilder agendaProgressBuilder;

    @BeforeMethod
    public void setup()
    {
        agendaProgressBuilder = new AgendaProgressBuilder();
    }

    @DataProvider
    public Object[][] maximumAttemptsAgendaProvider()
    {
        return new Object[][]
            {
                {null, AgendaProgress.DEFAULT_MAX_ATTEMPTS},
                {createAgenda(false, null), AgendaProgress.DEFAULT_MAX_ATTEMPTS},
                {createAgenda(true, null), AgendaProgress.DEFAULT_MAX_ATTEMPTS},
                {createAgenda(true, 15), 15}
            };
    }

    @Test(dataProvider = "maximumAttemptsAgendaProvider")
    public void testConfigureMaximumAttempts(Agenda agenda, final Integer EXPECTED_MAXIMUM)
    {
        agendaProgressBuilder.configureMaximumAttempts(agenda);
        Assert.assertEquals(agendaProgressBuilder.build().getMaximumAttempts(), EXPECTED_MAXIMUM);
    }

    private Agenda createAgenda(boolean createParams, Integer maximumAttempts)
    {
        Agenda agenda = new Agenda();
        if(createParams)
        {
            agenda.setParams(new ParamsMap());
            if(maximumAttempts != null)
                agenda.getParams().put(GeneralParamKey.maximumAttempts, maximumAttempts);
        }
        return agenda;
    }
}
