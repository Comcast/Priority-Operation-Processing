package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContextFactory;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * This is a generator for random sample payloads for parallel processing tests
 */
public class PayloadGenerator
{
    private static Logger logger = LoggerFactory.getLogger(PayloadGenerator.class);

    private int id = 0;
    private List<Operation> operations = new ArrayList<>();
    private Random random = new Random();
    JsonHelper jsonHelper = new JsonHelper();

    @Test(enabled = false)
    public String generatePayload()
    {
        addSampleOperation(40);
        addDependentOps(25);
        Agenda agenda = new Agenda();
        agenda.setOperations(operations);
        logger.info(jsonHelper.getPrettyJSONString(agenda));
        return jsonHelper.getJSONString(agenda);
    }

    @Test(enabled = false, invocationCount = 1)
    public void runTest()
    {
        logger.warn(System.getProperty("user.dir"));
        LaunchDataWrapper launchDataWrapper = new DefaultLaunchDataWrapper
            (
                new String[]
                    {
                        "-launchType",
                        "local",
                        "-externalLaunchType",
                        "local",
                        "-propFile",
                        "../package/local/config/external.properties"
                    }
            );
        launchDataWrapper.setEnvironmentRetriever(new FieldRetriever()
        {
            @Override
            public String getField(String s)
            {
                if(s.equals(HandlerField.PAYLOAD.name()))
                {
                    return generatePayload();
                }
                return null;
            }

            @Override
            public String getField(String s, String s1)
            {
                return null;
            }

            @Override
            public boolean isFieldSet(String s)
            {
                return false;
            }
        });

        new ParallelOperationAgendaProcessor(launchDataWrapper, new ExecutorContextFactory(launchDataWrapper).createOperationContext()).execute();
    }

    protected void addSampleOperation(int count)
    {
        IntStream.range(0, count).forEach(i ->
        {
            id++;
            Operation operation = new Operation();
            operation.setName("Sample." + id);
            operation.setType("sample");
            operation.setId(Integer.toString(id));
            operation.setPayload(getPayload("This is a custom log message"));
            operations.add(operation);
        });
    }

    protected void addDependentOps(int count)
    {
        IntStream.range(0, count).forEach(i ->
        {
            id++;
            Operation dependency = operations.get(random.nextInt(operations.size()));

            Operation operation = new Operation();
            operation.setName("Sample." + id);
            operation.setType("sample");
            operation.setId(Integer.toString(id));
            operation.setPayload(getPayload(String.format("@@%1$s.out::/actionData", dependency.getName())));
            operations.add(operation);
        });
    }

    protected Object getPayload(String logMessage)
    {
        try
        {
            return jsonHelper.getObjectMapper().readTree(String.format("{\"actions\":[{\"action\":\"log\",\"paramsMap\":{\"sleepMilliseconds\":3000,\"logMessage\":\"" +
                "%1$s\"}}]," +
                "\"resultPayload\":{\"actionData\":\"firstAction A\"}}", logMessage));
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
