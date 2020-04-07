package com.comcast.pop.endpoint.test.manual;

import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.test.base.EndToEndTestBase;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

public class ManualTest extends EndToEndTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(ManualTest.class);
    private static final String JSON_AGENDA_FILE = "sampleAgenda.json";

    @Test
    public void submitAgenda() throws Exception
    {
        final String LINK_ID = UUID.randomUUID().toString();

        ObjectClient<Agenda> agendaClient = new HttpObjectClient<>(agendaUrl, httpUrlConnectionFactory, Agenda.class);
        ObjectClient<AgendaProgress> agendaProgressClient = new HttpObjectClient<>(agendaProgressUrl, httpUrlConnectionFactory, AgendaProgress.class);
        String rawJson = getStringFromResourceFile(JSON_AGENDA_FILE);
        Agenda templateAgenda = jsonHelper.getObjectFromString(rawJson, Agenda.class);
        templateAgenda.setCustomerId(testCustomerId);
        templateAgenda.setLinkId(LINK_ID);
        DataObjectResponse<Agenda> response = agendaClient.persistObject(templateAgenda);

        verifyNoError(response);
        Assert.assertNotNull(response.getFirst());
        Agenda agenda = response.getFirst();

        waitOnStatus(agenda.getLinkId(), ProcessingState.COMPLETE);

        AgendaProgress agendaProgress = agendaProgressClient.getObject(agenda.getProgressId()).getFirst();

        logger.info("Final Progress : {}", jsonHelper.getPrettyJSONString(agendaProgress));
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
