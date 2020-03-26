package com.theplatform.commandbuilder.impl.command;

import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;
import com.theplatform.commandbuilder.impl.command.api.Phase.PhaseInterval;
import com.theplatform.commandbuilder.impl.command.api.Phase.UnknownPhase;
import com.theplatform.commandbuilder.impl.validate.exception.CommandValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.theplatform.commandbuilder.impl.validate.rules.RuleFactoryUtil.FORBIDDEN_CHARACTERS;

public abstract class AbstractExternalCommand implements ExternalCommand
    {
        private final Logger logger = LoggerFactory.getLogger(AbstractExternalCommand.class);
        protected final String programName;
        private PhaseInterval phaseInterval = UnknownPhase.Unknown;
        double phaseFraction;
        private String progressFormat = "-"+
                ProgressTokens.separator.getToken() +
                ProgressTokens.phaseKey.getToken() +
                ProgressTokens.keyValueSeparator.getToken() +
                "%s" +
                ProgressTokens.separator.getToken() +
                ProgressTokens.progressKey.getToken() +
                ProgressTokens.keyValueSeparator.getToken() +
                "%.1f" +
                ProgressTokens.separator.getToken();

    protected AbstractExternalCommand(String programName)
        {
            this.programName = programName;
        }

        @Override
        public String getProgramName()
        {
            return programName;
        }

        @Override
        public String toCommandString()
        {
            return String.join(" ", secure(getProgramArgumentList()));
        }

        protected List<String> secure(List<String> programArgumentList)
        {
            if(!FORBIDDEN_CHARACTERS.test(programArgumentList.toArray(new String[0])))
            {
                String msg = String.format("Argument for %s has forbidden character",getProgramName());
                logger.error(msg);
                throw new CommandValidationException(msg);
            }
            return programArgumentList;
        }

        @Override
        public String getProgress()
        {
            Double progressPercent = calcuatePercentProgress();
            String phase = phaseInterval.getPhase();

            return String.format(progressFormat, phase, progressPercent);
        }

        private Double calcuatePercentProgress()
        {
            // todo confirm that values are defined?
             return 100. * (phaseFraction*(phaseInterval.getMaxProgress() - phaseInterval.getMinProgress()) + phaseInterval.getMinProgress());
        }

        @Override
        public void setPhaseInterval(PhaseInterval phaseInterval)
        {
            this.phaseInterval = phaseInterval;
        }

        @Override
        public void setPhaseFraction(double phaseFraction)
        {
            this.phaseFraction = phaseFraction;
        }
}
