package com.theplatform.commandbuilder.impl.build.handler.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConnnectDataFactory
{
    private static Logger logger = LoggerFactory.getLogger(ConnnectDataFactory.class);
    private final List<ConnectionBuilder> connectionBuilderList;

    public ConnnectDataFactory( ConnectionBuilder ... connectionBuilders)
    {
        connectionBuilderList = Arrays.asList(connectionBuilders);
    }

    public Connect makeConnectData(ConnectData connectData)
    {
        Optional<ConnectionBuilder> builder = connectionBuilderList.stream().filter(td -> td.isType(connectData)).findFirst();

        if(!builder.isPresent())
        {
            logger.warn("No connection builder detected; providing noop connection builder.");
            return connectData;
        }
        return builder.get().build(connectData);
    }
}
