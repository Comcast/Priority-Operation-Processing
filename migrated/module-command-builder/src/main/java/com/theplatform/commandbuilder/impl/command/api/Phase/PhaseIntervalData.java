package com.theplatform.commandbuilder.impl.command.api.Phase;

public interface PhaseIntervalData
{
    String getProgress();

    void setPhaseFraction(double phaseFraction);

    void setPhaseInterval(PhaseInterval phaseInterval);
}
