package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileProgressLoader extends ProgressLoader
{
    private static Logger logger = LoggerFactory.getLogger(FileProgressLoader.class);
    private String filePath;

    public FileProgressLoader(String filePath, ExecutorContext executorContext)
    {
        super(executorContext);
        this.filePath = filePath;
    }

    @Override
    public AgendaProgress loadProgress()
    {
        try
        {
            String agendaProgressJson = new String(Files.readAllBytes(Paths.get(filePath)));
            return parseAgendaProgress(agendaProgressJson);
        }
        catch(IOException e)
        {
            logger.error(String.format("Error reading file %1$s", filePath), e);
        }
        return null;
    }
}
