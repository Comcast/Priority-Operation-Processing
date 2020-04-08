package com.comcast.pop.handler.executor.impl.processor.parallel;

import com.comcast.pop.handler.executor.impl.context.ExecutorContextFactory;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.retriever.DefaultLaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
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
    public void logGeneratedPayload()
    {
        logger.info(jsonHelper.getPrettyJSONString(generatePayload()));
    }

    public String generatePayload()
    {
        addSampleOperation(40);
        addDependentOps(25);
        Agenda agenda = new Agenda();
        agenda.setOperations(operations);
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
        launchDataWrapper.setEnvironmentRetriever(new EnvironmentFieldRetriever()
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

        new ParallelOperationAgendaProcessor(new ExecutorContextFactory(launchDataWrapper).createOperationContext()).execute();
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
            operation.setPayload(getPayload(String.format("@<%1$s.out::/actionData>", dependency.getName())));
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
