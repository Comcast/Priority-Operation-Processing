package com.comcast.pop.cp.endpoint.agenda.reporter;

import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AgendaValidator extends AgendaData
{

    void validatePattern(String msg)
    {
        validateAgendaFields(msg);
        assertThat(msg).contains("%s");
    }

    void validateLogs(String msg)
    {
        validateAgendaFields(msg);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(msg).contains(elapsedTime);
            softly.assertThat(msg).contains(conclusionStatus);
        });
    }

    void validateAgendaFields(String msg)
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
    };

}
