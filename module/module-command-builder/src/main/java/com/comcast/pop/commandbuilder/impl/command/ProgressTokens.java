package com.comcast.pop.commandbuilder.impl.command;

public enum ProgressTokens
{
    separator(" || "),
    keyValueSeparator(" : "),
    phaseKey("Phase"),
    progressKey("Percent done");

    private String token;

    ProgressTokens(String token)
    {

        this.token = token;
    }

    public String getToken()
    {
        return token;
    }
}
