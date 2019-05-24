package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import org.assertj.core.api.SoftAssertions;

public class AgendaValidator extends AgendaData
{

    void validateLogs(String msg)
    {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(msg).contains(prefix);
            softly.assertThat(msg).contains(testCid);
            softly.assertThat(msg).contains(agendaId);
            softly.assertThat(msg).contains(linkId);
            softly.assertThat(msg).contains(testOperation);
            softly.assertThat(msg).contains(operationType);
            softly.assertThat(msg).contains(widthKey);
            softly.assertThat(msg).contains(withvalue);
        });
    }
}
