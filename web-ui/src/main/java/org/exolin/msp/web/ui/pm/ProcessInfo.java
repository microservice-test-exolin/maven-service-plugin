package org.exolin.msp.web.ui.pm;

import java.util.List;

/**
 *
 * @author tomgk
 */
public class ProcessInfo
{
    private final String service;
    private final Process process;
    private final List<String> cmd;
    private final String title;
    private final long startTime;

    public ProcessInfo(String service, Process process, List<String> cmd, String title, long startTime)
    {
        this.service = service;
        this.process = process;
        this.cmd = cmd;
        this.title = title;
        this.startTime = startTime;
    }

    public String getService()
    {
        return service;
    }

    public Process getProcess()
    {
        return process;
    }

    public List<String> getCmd()
    {
        return cmd;
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