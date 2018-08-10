package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.process.helper.ProcessHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * This local executor requires mediainfo to be in the path somewhere...
 */
public class LocalAgendaExecutor extends AgendaExecutor
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProcessHelper processHelper;

    public LocalAgendaExecutor(String filePath)
    {
        super(filePath);
        processHelper = new ProcessHelper();
    }

    @Override
    public List<String> execute()
    {
        try
        {
            String[] commandLineArgs = new String[] { "mediainfo", "--Output=XML", "-f", getFilePath() };
            logger.debug("Launching: {}", String.join(" ", commandLineArgs));
            return processHelper.getOutput(
                commandLineArgs,
                null,
                null
            ).getOutputLines();
        }
        catch(IOException e)
        {
            throw new AgendaExecutorException("Failed to execute local mediainfo", e);
        }
    }

    public ProcessHelper getProcessHelper()
    {
        return processHelper;
    }

    public void setProcessHelper(ProcessHelper processHelper)
    {
        this.processHelper = processHelper;
    }
}