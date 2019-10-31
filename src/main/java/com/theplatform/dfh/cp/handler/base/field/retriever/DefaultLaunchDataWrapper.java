package com.theplatform.dfh.cp.handler.base.field.retriever;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.DefaultArgumentProvider;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Default implmentation of the LaunchDataWrapper (basic override of properties file and paylod)
 */
public class DefaultLaunchDataWrapper extends LaunchDataWrapper
{
    private static final String DEFAULT_PROPERTIES_PATH = "/app/config/external.properties";

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
        String payloadFile = getArgumentRetriever().getField(HandlerArgument.PAYLOAD_FILE.getArgumentName(), null);

        if(payloadFile != null)
        {
            try
            {
                return new String(Files.readAllBytes(Paths.get(payloadFile)), StandardCharsets.UTF_8);
            }
            catch(IOException e)
            {
                throw new RuntimeException(String.format("Failed to load payload from: %1$s", payloadFile), e);
            }
        }
        else
        {
            return getEnvironmentRetriever().getField(HandlerField.PAYLOAD.name());
        }
    }

    protected static String getPropertiesPath(FieldRetriever argumentRetriever)
    {
        return argumentRetriever.getField(HandlerArgument.PROP_FILE.getArgumentName(), DEFAULT_PROPERTIES_PATH);
    }
}
