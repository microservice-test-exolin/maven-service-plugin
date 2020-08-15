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
    
    public ProcessInfo(String service, String name, long startTime, List<String> cmd, String title)
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
    
    public String getService()
    {
        return service;
    }

    public String getName()
    {
        return name;
    }
    
    public Process getProcess()
    {
        return process;
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
        if(!process.isAlive())
            return -1;

        return System.currentTimeMillis() - startTime;
    }
}
