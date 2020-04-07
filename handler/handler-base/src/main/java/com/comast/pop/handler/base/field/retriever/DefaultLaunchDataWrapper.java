package com.comast.pop.handler.base.field.retriever;

import com.comast.pop.handler.base.field.api.args.HandlerArgument;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comast.pop.handler.base.field.retriever.argument.ArgumentRetriever;
import com.comast.pop.handler.base.field.retriever.argument.DefaultArgumentProvider;
import com.comast.pop.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Default implmentation of the LaunchDataWrapper (basic override of properties file and paylod)
 */
public class DefaultLaunchDataWrapper extends LaunchDataWrapper
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String DEFAULT_PROPERTIES_PATH = "/app/config/external.properties";
    private JsonHelper jsonHelper = new JsonHelper();

    public DefaultLaunchDataWrapper(String[] args)
    {
        this(new ArgumentRetriever(new DefaultArgumentProvider(args)));

    }

    public DefaultLaunchDataWrapper(ArgumentRetriever argumentRetriever, EnvironmentFieldRetriever environmentRetriever, PropertyRetriever propertyRetriever)
    {
        setArgumentRetriever(argumentRetriever);
        setEnvironmentRetriever(environmentRetriever);
        setPropertyRetriever(propertyRetriever);
    }

    public DefaultLaunchDataWrapper(ArgumentRetriever argumentRetriever)
    {
        setArgumentRetriever(argumentRetriever);
        setEnvironmentRetriever(new EnvironmentFieldRetriever());
        String propertiesPath = getPropertiesPath(getArgumentRetriever());
        setPropertyRetriever(new PropertyRetriever(propertiesPath));
    }

    @Override
    public String getPayload()
    {
        return getStringFromFields(HandlerArgument.PAYLOAD_FILE, HandlerField.PAYLOAD);
    }

    @Override
    public OperationProgress getLastOperationProgress()
    {
        return getLastProgressObject(OperationProgress.class);
    }

    @Override
    public <T> T getLastProgressObject(Class<T> objectClass)
    {
        String operationProgressJson = getStringFromFields(HandlerArgument.LAST_PROGRESS_FILE, HandlerField.LAST_PROGRESS);

        if(operationProgressJson == null)
            return null;

        try
        {
            return jsonHelper.getObjectFromString(operationProgressJson, objectClass);
        }
        catch(JsonHelperException e)
        {
            logger.warn("Unable to read the last progress object. Defaulting to null.", e);
        }
        return null;
    }

    @Override
    public <T> T getLastOperationProgressParam(String paramName, Class<T> objectClass)
    {
        String operationProgressJson = getStringFromFields(HandlerArgument.LAST_PROGRESS_FILE, HandlerField.LAST_PROGRESS);

        if(operationProgressJson == null)
            return null;

        try
        {
            OperationProgress operationProgress = jsonHelper.getObjectFromString(operationProgressJson, OperationProgress.class);
            if(operationProgress == null || operationProgress.getParams() == null || !operationProgress.getParams().containsKey(paramName))
                return null;

            return jsonHelper.getObjectMapper().convertValue(operationProgress.getParams().get(paramName), objectClass);
        }
        catch(JsonHelperException e)
        {
            logger.warn("Unable to read the last progress object. Defaulting to null.", e);
        }
        return null;
    }

    /**
     * Attempts to read a string from the command line argument file specified defaulting to the contents of the environment variable specified
     * @param commandLineArg The argument to get the file name from
     * @param environmentVar The environment var to read the value from (fallback)
     * @return The string contained in the file, the contents of the environment variable, or null (in that order)
     */
    protected String getStringFromFields(HandlerArgument commandLineArg, HandlerField environmentVar)
    {
        String payload = getStringFromFileArg(commandLineArg);

        return payload == null
            ? getEnvironmentRetriever().getField(environmentVar.name())
            : payload;
    }

    /**
     * Retrieves the raw string from the file associated with the specified command line arg (assumes UTF-8)
     * @param commandLineArg The argument to extract from
     * @return The string from the file contents or null
     */
    protected String getStringFromFileArg(HandlerArgument commandLineArg)
    {
        String payloadFile = getArgumentRetriever().getField(commandLineArg.getArgumentName(), null);
        if(payloadFile != null)
        {
            try
            {
                return new String(Files.readAllBytes(Paths.get(payloadFile)), StandardCharsets.UTF_8);
            }
            catch (IOException e)
            {
                throw new RuntimeException(String.format("Failed to load data from: %1$s", payloadFile), e);
            }
        }
        return null;
    }

    protected static String getPropertiesPath(FieldRetriever argumentRetriever)
    {
        return argumentRetriever.getField(HandlerArgument.PROP_FILE.getArgumentName(), DEFAULT_PROPERTIES_PATH);
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
