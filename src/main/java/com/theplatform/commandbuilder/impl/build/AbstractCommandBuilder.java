package com.theplatform.commandbuilder.impl.build;

import com.theplatform.commandbuilder.api.CommandBuild;
import com.theplatform.commandbuilder.api.CommandExceptionFactory;
import com.theplatform.commandbuilder.api.CommandValidate;
import com.theplatform.commandbuilder.impl.validate.NoopCommandValidator;
import com.theplatform.commandbuilder.impl.validate.exception.CommandValidationExceptionFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCommandBuilder implements CommandBuild
{
    private static final String RAW = "";
    private static final String CR = System.getProperty("line.separator");

    private List<StringBuilder> builders = new LinkedList<>();
    private StringBuilder builder = new StringBuilder();
    private StringBuilder toStringBuilder = new StringBuilder();
    private CommandValidate validator = new NoopCommandValidator();
    private CommandExceptionFactory commandExceptionFactory = new CommandValidationExceptionFactory("Invalid command");
    String separator = RAW;



    @Override
    public CommandBuild add(String command)
    {
        validate(command);
        if(builder.length() > 0)
        {
            builder.append(separator);
        }
        builder.append(command);
        if(toStringBuilder.length() > 0)
        {
            toStringBuilder.append(separator);
            toStringBuilder.append(CR);
        }
        toStringBuilder.append(command);

        return this;
    }

    private void validate(String command)
    {
        if(!validator.validate(command))
        {
            throw commandExceptionFactory.makeException(command);
        }

    }

    @Override
    public CommandBuild addAll(String[] commands)
    {
        for(String command: commands)
        {
            add(command);
        }
        return this;
    }

    @Override
    public CommandBuild newBlock()
    {
        if(builder.length() > 0)
        {
             builders.add(builder);
             builder = new StringBuilder();
        }
        return this;
    }

    @Override
    public CommandBuild setExecRaw()
    {
        separator = RAW;
        return this;
    }


    @Override
    public CommandBuild setCustomSeparator(String separator)
    {
        this.separator=separator;
        return this;
    }


    @Override
    public CommandBuild setValidator(CommandValidate validator)
    {
        this.validator = validator;
        return this;
    };

    @Override
    public CommandBuild setCommandExceptionFactory(CommandExceptionFactory commandExceptionFactory)
    {
        this.commandExceptionFactory = commandExceptionFactory;
        return this;
    };

    @Override
    public String[] build()
    {
        if(builder.length() > 0)
        {
            builders.add(builder);
        }
        String[] commandBlocks = new String[builders.size()];
        int ix = 0;
        for(StringBuilder nextBuilder: builders)
        {
            commandBlocks[ix++] = nextBuilder.toString();
        }
        return commandBlocks;
    }

    @Override
    public String toString()
    {
        return toStringBuilder.toString();
    }
}
