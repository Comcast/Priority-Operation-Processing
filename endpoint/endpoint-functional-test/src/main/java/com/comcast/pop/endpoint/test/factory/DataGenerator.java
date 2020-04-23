package com.comcast.pop.endpoint.test.factory;

import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaParameter;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.TransformRequest;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.api.facility.SchedulingAlgorithm;
import com.comcast.pop.api.input.InputFileResource;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;

import java.util.*;
import java.util.stream.Collectors;

public class DataGenerator
{
    private static final String OPERATION_TYPE = "sample";
    public static ResourcePool generateResourcePool(String customerId)
    {
        ResourcePool resourcePool = new ResourcePool();
        resourcePool.setId("2u9490283912832109283091");
        resourcePool.setCustomerId(customerId);
        resourcePool.setTitle("ResourcePool-" + UUID.randomUUID().toString());
        resourcePool.setInsightIds(new ArrayList<>());
        return resourcePool;
    }
    public static Insight generateInsight(String customerId)
    {
        Insight insight = new Insight();
        insight.setId("9384932rfdiofjwoiejf");
        insight.setQueueName("aws:queue:my-queue");
        insight.setQueueSize(900);
        insight.setTitle("Insight-" + UUID.randomUUID().toString());
        insight.setCustomerId(customerId);
        insight.setSchedulingAlgorithm(SchedulingAlgorithm.FirstInFirstOut);
        return insight;
    }
    public static Customer generateCustomer(String customerId)
    {
        Customer customer = new Customer();
        customer.setBillingCode("My billing code");
        customer.setId("982048329048239jfkdsl");
        customer.setTitle("Customer-" + UUID.randomUUID().toString());
        customer.setCustomerId(customerId);
        return customer;
    }

    public static AgendaTemplate generateAgendaTemplate(String customerId)
    {
        AgendaTemplate agendaTemplate = new AgendaTemplate();
        agendaTemplate.setCustomerId(customerId);
        agendaTemplate.setCid("testCid" + UUID.randomUUID().toString());
        agendaTemplate.setTitle("AgendaTemplate-" + UUID.randomUUID().toString());
        return agendaTemplate;
    }

    public static Agenda generateAgenda(String customerId)
    {
        Agenda agenda = new Agenda();
        agenda.setJobId("theJob");
        agenda.setLinkId(UUID.randomUUID().toString());
        agenda.setCustomerId(customerId);
        agenda.setCid("testCid" + UUID.randomUUID().toString());
        Operation operation = new Operation();
        operation.setId("op1.id");
        operation.setType(OPERATION_TYPE);
        operation.setName("op1.name");
        operation.setPayload(new HashMap<>());
        agenda.setOperations(Collections.singletonList(operation));
        return agenda;
    }

    public static Agenda createNonRunningLogAgenda(String customerId, String... opNames)
    {
        Agenda agenda = createAgenda(customerId, opNames);
        agenda.getParams().put(GeneralParamKey.doNotRun, null);
        return agenda;
    }

    public static Agenda createAgenda(String customerId, String... opNames)
    {
        Agenda agenda = new Agenda();
        agenda.setCustomerId(customerId);
        if (opNames != null)
        {
            agenda.setOperations(Arrays.stream(opNames).map(name ->
            {
                Operation operation = new Operation();
                operation.setName(name);
                operation.setType(OPERATION_TYPE);
                return operation;
            }).collect(Collectors.toList()));
        }
        agenda.setParams(new ParamsMap());
        return agenda;
    }

    public static final String AGENDA_ID = "86e2a373-9c16-4052-8580-a90ee5684962";

    public static RerunAgendaRequest generateSimpleReigniteAgendaRequest(String agendId)
    {

        List<String> params = Arrays.asList(
                RerunAgendaParameter.RESET_ALL.getParameterName()
        );

        return new RerunAgendaRequest(agendId, params);
    }

    public static final String AGENDA_TEMPLATE_ID = "899f2241-3877-4301-b3d2-aa7e4274e499";

    public static RunAgendaRequest generateSimpleIgniteAgendaRequest(String agendaTemplateId)
    {

        RunAgendaRequest request = new RunAgendaRequest();
        request.setAgendaTemplateId(agendaTemplateId);
        request.setPayload("{\"logMessage\":\"This is the message from the input payload.\"}");

        return request;
    }

    public static AgendaProgress getAgendaProgress(String customerId)
    {
        AgendaProgress progress = new AgendaProgress();
        progress.setCustomerId(customerId);
        progress.setLinkId("AgendaProgress-" + UUID.randomUUID().toString());
        progress.setProcessingState(ProcessingState.WAITING);
        return progress;
    }

    public static OperationProgress getOperationProgress(String customerId, String agendaProgressId)
    {
        OperationProgress progress = new OperationProgress();
        progress.setAgendaProgressId(agendaProgressId);
        progress.setCustomerId(customerId);
        return progress;
    }

    public static Operation createOperation(String operationType, String operationName)
    {
        Operation operation = new Operation();
        operation.setType(operationType);
        operation.setName(operationName);
        return operation;
    }

    public static TransformRequest getTransformRequest(String customerId, String cid)
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setExternalId(UUID.randomUUID().toString());
        transformRequest.setCustomerId(customerId);
        transformRequest.setCid(cid);
        transformRequest.setAgendaTemplateTitle("Accelerate");
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put("Hi", "There");
        transformRequest.setParams(paramsMap);
        InputFileResource inputFileResource = new InputFileResource();
        inputFileResource.setUrl("/file.mp4");
        transformRequest.setInputs(Collections.singletonList(inputFileResource));
        return transformRequest;
    }
}
