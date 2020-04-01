package com.comcast.fission.endpoint.test.factory;

import com.comcast.fission.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.ReigniteAgendaParameter;
import com.comcast.fission.endpoint.api.agenda.ReigniteAgendaRequest;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.facility.SchedulingAlgorithm;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.*;
import java.util.stream.Collectors;

public class DataGenerator
{
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
        operation.setType("testOperation");
        operation.setName("op1.name");
        operation.setPayload(new HashMap<>());
        agenda.setOperations(Collections.singletonList(operation));
        return agenda;
    }

    public static Agenda createNonRunningLogAgenda(String customerId, String... opNames)
    {
        Agenda agenda = new Agenda();
        agenda.setCustomerId(customerId);
        if (opNames != null)
        {
            agenda.setOperations(Arrays.stream(opNames).map(name ->
            {
                Operation operation = new Operation();
                operation.setName(name);
                operation.setType("testOperation");
                return operation;
            }).collect(Collectors.toList()));
        }
        ParamsMap paramsMap = new ParamsMap();
        // These agendas are not intended to execute!
        paramsMap.put(GeneralParamKey.doNotRun, null);
        agenda.setParams(paramsMap);
        return agenda;
    }

    public static final String AGENDA_ID = "86e2a373-9c16-4052-8580-a90ee5684962";

    public static ReigniteAgendaRequest generateSimpleReigniteAgendaRequest(String agendId)
    {

        List<String> params = Arrays.asList(
                ReigniteAgendaParameter.RESET_ALL.getParameterName()
        );

        return new ReigniteAgendaRequest(agendId, params);
    }

    public static final String AGENDA_TEMPLATE_ID = "899f2241-3877-4301-b3d2-aa7e4274e499";

    public static IgniteAgendaRequest generateSimpleIgniteAgendaRequest(String agendaTemplateId)
    {

        IgniteAgendaRequest request = new IgniteAgendaRequest();
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
