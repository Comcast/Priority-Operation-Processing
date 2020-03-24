package com.theplatform.dfh.process.helper;

/**
 * This class inputStream used to capture stream output from an external process
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class StreamGobbler extends Thread
{
    private InputStream inputStream;
    private List<String> lines;
    private Boolean readComplete;

    StreamGobbler(InputStream inputStream)
    {
        this.inputStream = inputStream;

    }

    public void run()
    {
        lines = new LinkedList<>();
        readComplete = false;
        try
        {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            readComplete = true;
        }
    }

    /**
     * Waits for the stream reader to complete reading before returning the result
     * @return result string
     */
    public List<String> getResult()
    {
        while (!readComplete)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
                // swallow and exit;
            }
        }
        return lines;
    }
}