package com.comcast.pop.commandbuilder.api;


public interface CommandBuild
{
    CommandBuild add(String command);

    CommandBuild addAll(String[] command);

    CommandBuild newBlock();

    CommandBuild setExecRaw();
    CommandBuild setExecAll();
    CommandBuild setExecAny();
    CommandBuild setExecUntilFail();

    CommandBuild setCustomSeparator(String separator);

    CommandBuild setValidator(CommandValidate validator);

    CommandBuild setCommandExceptionFactory(CommandExceptionFactory exceptionFactory);

    String[] build();
}
