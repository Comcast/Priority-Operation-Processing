package com.comcast.pop.process.helper;

import java.util.List;

public class ProcessOutput
{
    private List<String> outputLines;
    private List<String> errorLines;

    public ProcessOutput(List<String> outputLines, List<String> errorLines)
    {
        this.outputLines = outputLines;
        this.errorLines = errorLines;
    }

    public List<String> getOutputLines()
    {
        return outputLines;
    }

    public void setOutputLines(List<String> outputLines)
    {
        this.outputLines = outputLines;
    }

    public List<String> getErrorLines()
    {
        return errorLines;
    }

    public void setErrorLines(List<String> errorLines)
    {
        this.errorLines = errorLines;
    }
}
