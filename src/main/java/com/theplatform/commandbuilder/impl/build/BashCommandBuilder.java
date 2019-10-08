package com.theplatform.commandbuilder.impl.build;

import com.theplatform.commandbuilder.api.CommandBuild;


public class BashCommandBuilder extends AbstractCommandBuilder
{
    private static final String ALL = " ; ";
    private static final String ANY = " || ";
    private static final String UNTIL = " && ";


    /**
     * Execute all commands regardless of success (0 return value) or failure (non-zero return value)
     * @return builder
     */
        @Override
    public CommandBuild setExecAll()
    {
        separator = ALL;
        return this;
    }

    /**
     * Execute  commands until first success (zero return value)
     * @return builder
     */
    @Override
    public CommandBuild setExecAny()
    {
        separator = ANY;
        return this;
    }

    /**
     * Execute  commands until first failure (non-zero return value)
     * @return builder
     */
    @Override
    public CommandBuild setExecUntilFail()
    {
        separator = UNTIL;
        return this;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }
}
