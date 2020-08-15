package org.exolin.msp.service.pm;

import java.util.List;

/**
 *
 * @author tomgk
 */
public class ProcessInfo
{
    private final String service;
    private final String name;
    private final long startTime;

    private Process process;
    private final List<String> cmd;
    private final String title;
    private Integer exitCode;
    
    public ProcessInfo(String service, String name, long startTime, List<String> cmd, String title, Integer exitCode)
    {
        this.service = service;
        this.name = name;
        this.startTime = startTime;
        this.cmd = cmd;
        this.title = title;
    }

    public void setProcess(Process process)
    {
        this.process = process;
    }

    public Integer getExitCode()
    {
        return exitCode;
    }

    void updateExitCode()
    {
        if(process != null && !process.isAlive())
            exitCode = process.exitValue();
    }
    
    public String getService()
    {
        return service;
    }

    public String getName()
    {
        return name;
    }
    
    public boolean isAlive()
    {
        return process != null ? process.isAlive() : true/*um nicht zu früh zu enternt zu werden*/;
    }

    void destroyForcibly()
    {
        if(process != null)
            process.destroyForcibly();
    }

    public List<String> getCmd()
    {
        return cmd;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public String getTitle()
    {
        return title;
    }

    public long getRuntime()
    {
        if(!isAlive())
            return -1;

        return System.currentTimeMillis() - startTime;
    }
}
