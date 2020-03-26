package com.theplatform.commandbuilder.impl.command.api.Phase;

public enum UnknownPhase  implements PhaseInterval
{
    Unknown(0.0, 0.0);

    private final double min;
    private final double max;

    UnknownPhase(double min, double max)
    {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getPhase()
    {
        return name();
    }

    @Override
    public double getMinProgress()
    {
        return min;
    }

    @Override
    public double getMaxProgress()
    {
        return max;
    }
}
