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

    private final Process process;
    private final List<String> cmd;
    private final String title;

    public ProcessInfo(String service, String name, long startTime, Process process, List<String> cmd, String title)
    {
        this.service = service;
        this.name = name;
        this.startTime = startTime;
        this.process = process;
        this.cmd = cmd;
        this.title = title;
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
