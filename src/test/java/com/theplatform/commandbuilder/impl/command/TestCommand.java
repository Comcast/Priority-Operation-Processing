package com.theplatform.commandbuilder.impl.command;


import java.util.LinkedList;
import java.util.List;

public class TestCommand extends AbstractExternalCommand
{
    private List<String> programArugmentList;

    public TestCommand(String programName, List<String> programArugmentList) {
        super(programName);
        this.programArugmentList = programArugmentList;
    }

    @Override
    public String toScrubbedCommandString() {
        List<String> scrubbedList = new LinkedList<>();
        scrubbedList.addAll(programArugmentList);
        scrubbedList.add("scrubberArg");
        return String.join(" ", scrubbedList);
    }

    @Override
    public List<String> getProgramArgumentList() {
        return programArugmentList;
    }
}
