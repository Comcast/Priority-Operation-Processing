package com.comcast.pop.commandbuilder.impl.build.handler.connect;

import com.comcast.pop.commandbuilder.impl.command.api.CommandGeneratorBuilder;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.s3.ConnectGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Extensible list of connection-command-builders (e.g. S3CommmandGeneratorBuilder, test command builders, etc.); defaults to NoopConnectCommandGenerator
 */
public class ConnectionCommandBuilderFactory
{
    private static Logger logger = LoggerFactory.getLogger(ConnectionCommandBuilderFactory.class);
    private final ConnectData input;
    private final List<CommandGeneratorBuilder<ConnectGenerator, ConnectData>> connectionCommandBuilders;

    public ConnectionCommandBuilderFactory(ConnectData input, CommandGeneratorBuilder<ConnectGenerator, ConnectData> ... commandGeneratorBuilderTypes)
    {
        this.input = input;
        connectionCommandBuilders = Arrays.asList(commandGeneratorBuilderTypes);
    }

    public Optional<ConnectGenerator> makeConnectionCommandBuilder()
    {
        Optional<CommandGeneratorBuilder<ConnectGenerator, ConnectData>> builder = connectionCommandBuilders.stream().filter(td -> td.isType(input)).findFirst();

        if(!builder.isPresent())
        {
            logger.warn("No connection command generator detected; providing noop command generator builder.");
        return Optional.of(new NoopConnectCommandGenerator(input));
        }
        return builder.get().makeCommandGenerator(input);
    }
}
