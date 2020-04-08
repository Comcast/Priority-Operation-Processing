package com.comcast.pop.commandbuilder.impl.command;

import com.comcast.pop.commandbuilder.impl.command.api.CommandList;
import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseInterval;
import com.comcast.pop.commandbuilder.impl.command.api.ProgressFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class CommandListHelper<T extends ExternalCommand> implements CommandList<T>
{
    private final String NONESUCH_PHASE = "nonesuchphase348523457-3248523-8945";
    private List<T> externalCommands = new LinkedList<>();
    private List<String> commandPhases = new LinkedList<>();
    private Map<String, Double> phaseCount = new HashMap<>();

    @Override
    public void add(T externalCommand, PhaseInterval phaseInterval)
    {
        addPhase(phaseInterval.getPhase());
        externalCommand.setPhaseInterval(phaseInterval);
        externalCommands.add(externalCommand);
    }

    @Override
    public void add(ProgressFactory<T> progressFactory)
    {
        List<T> commands = progressFactory.getCommands();
        PhaseInterval[] progressData = progressFactory.getProgressData().toArray(new PhaseInterval[0]);

        for(int i = 0; i < commands.size(); i++)
        {
            add( commands.get(i), progressData[i]);
        }
    }

    @Override
    public void clear()
    {
        externalCommands.clear();
        commandPhases.clear();
        phaseCount.clear();
    }

    @Override
    public List<T> get()
    {
        calculateProgress();
        return new LinkedList<>(externalCommands);
    }

    private void addPhase(String phase)
    {
        commandPhases.add(phase);
        if(phaseCount.keySet().contains(phase))
        {
            phaseCount.put(phase, phaseCount.get(phase) + 1.0);
        }
        else
        {
            phaseCount.put(phase, 1.0);
        }
    }

    /**
     * Assumes that command phases are in singular intervals
     */
    private void calculateProgress()
    {
        ExternalCommand[] commands = externalCommands.toArray(new ExternalCommand[0]);
        String[] phases = commandPhases.toArray(new String[0]);
        double phaseIndexFromOne = 1.0;
        String phase = NONESUCH_PHASE;
        for(int i = 0; i < commands.length; i++)
        {
            phaseIndexFromOne = phase.equals(phases[i]) ? phaseIndexFromOne : 1;
            phase = phases[i];
            ExternalCommand command = commands[i];
            double fraction = phaseIndexFromOne++/phaseCount.get(phase);
            command.setPhaseFraction(fraction);
        }
    }
}
