package com.theplatform.commandbuilder.impl.command;

import com.theplatform.commandbuilder.impl.command.api.Phase.PhaseInterval;

public enum TestPhase implements PhaseInterval {

    Pretest(0., 0.3),
    Test(0.3, 0.7),
    Clean(0.7,1.0);

        private final double min;
        private final double max;

    TestPhase(double min, double max)
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
