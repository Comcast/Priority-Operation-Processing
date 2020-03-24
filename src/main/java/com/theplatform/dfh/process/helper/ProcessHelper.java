package com.theplatform.dfh.process.helper;

import java.io.File;
import java.io.IOException;

public class ProcessHelper
{
    private StreamGobblerFactory streamGobblerFactory;

    public ProcessHelper()
    {
        this.streamGobblerFactory = new StreamGobblerFactory();
    }

    /**
     * Synchronus method to collect the output and error output from a process launch.
     * See Runtime.exec for the parameter details
     * @param cmdarray array containing the command to call and its arguments.
     * @param envp array of strings, each element of which has environment variable settings in the
     *             format name=value, or null if the subprocess should inherit the environment of the current process.
     * @param dir the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process.
     * @return ProcessOutput with the results of the process output collection
     * @throws IOException Anything runtime.exec may throw
     */
    public ProcessOutput getOutput(String[] cmdarray, String[] envp, File dir) throws IOException
    {
        return getProcessOutput(Runtime.getRuntime().exec(cmdarray, envp, dir));
    }

    /**
     * Synchronus method to collect the output and error output from a process launch.
     * See Runtime.exec for the parameter details
     * @param cmd a specified system command.
     * @param envp array of strings, each element of which has environment variable settings in the
     *             format name=value, or null if the subprocess should inherit the environment of the current process.
     * @param dir the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process.
     * @return ProcessOutput with the results of the process output collection
     * @throws IOException Anything runtime.exec may throw
     */
    public ProcessOutput getOutput(String cmd, String[] envp, File dir) throws IOException
    {
        return getProcessOutput(Runtime.getRuntime().exec(cmd, envp, dir));
    }

    protected ProcessOutput getProcessOutput(Process process)
    {
        StreamGobbler errorGobbler = streamGobblerFactory.getStreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = streamGobblerFactory.getStreamGobbler(process.getInputStream());

        errorGobbler.start();
        outputGobbler.start();

        return new ProcessOutput(outputGobbler.getResult(), errorGobbler.getResult());
    }

    public void setStreamGobblerFactory(StreamGobblerFactory streamGobblerFactory)
    {
        this.streamGobblerFactory = streamGobblerFactory;
    }
}
