package com.theplatform.commandbuilder.impl.command.api;

import java.util.Optional;

public interface CommandGeneratorBuilder<GeneratorType, CommandType>
{
     boolean isType(CommandType input);
     Optional<GeneratorType> makeCommandGenerator(CommandType input);
}
