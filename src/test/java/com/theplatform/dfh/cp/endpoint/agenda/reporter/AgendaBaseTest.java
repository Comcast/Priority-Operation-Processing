package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AgendaBaseTest extends AgendaData
{
    protected AgendaReports[] agendaReports = {AgendaReports.CID, AgendaReports.AGENDA_ID, AgendaReports.AGENDA_TYPE,  AgendaReports.CUSTOMER_ID, AgendaReports.LINK_ID, AgendaReports.AGENDA_STATUS, AgendaReports.MILLISECONDS_IN_QUEUE, AgendaReports.OPERATION_PAYLOAD};
    protected TestAgendaReporter agendaReporter;
    protected TestLogger testLogger;
    protected AgendaValidator agendaValidator = new AgendaValidator();

    protected Agenda makeAgenda()
    {
        Agenda agenda = new Agenda();
        agenda.setCid(testCid);
        agenda.setId(agendaId);
        agenda.setCustomerId(owner);
        agenda.setLinkId(linkId);
        agenda.setOperations(makeOperations());
        agenda.setAddedTime(new Date(System.currentTimeMillis()));
        agenda.setParams(new ParamsMap());
        return agenda;
    }

    protected ParamsMap makeOperationParams()
    {
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(widthKey, withvalue);
        return paramsMap;
    }

    protected List<Operation> makeOperations()
    {
        List<Operation> opsList = new LinkedList<>();
        Operation operation = new Operation();
        operation.setName(testOperation);
        operation.setType(operationType);
        operation.setId(operationId);
        operation.setParams(makeOperationParams());
        opsList.add(operation);
        return opsList;
    }
    protected AgendaProgress makeAgendaProgress(String state)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(ProcessingState.COMPLETE);
        agendaProgress.setProcessingStateMessage(state);
        return agendaProgress;
    }
}
