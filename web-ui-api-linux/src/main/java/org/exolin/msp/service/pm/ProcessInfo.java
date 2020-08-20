package org.exolin.msp.service.pm;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessInfo
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInfo.class);
    
    private final String service;
    private final String name;
    private final long startTime;

    private Process process;
    private final Path workingDirectory;
    private final List<String> cmd;
    private final String title;
    private Long endTime;
    private Integer exitCode;
    private final String initiator;
    
    public ProcessInfo(String service, String name, long startTime, Path workingDirectory, List<String> cmd, String title, String initiator, Long endTime, Integer exitCode)
    {
        this.service = service;
        this.name = name;
        this.startTime = startTime;
        this.workingDirectory = workingDirectory;
        this.cmd = cmd;
        this.initiator = initiator;
        this.title = title;
        this.endTime = endTime;
        this.exitCode = exitCode;
    }

    public void setProcess(Process process)
    {
        this.process = Objects.requireNonNull(process);
    }

    public Integer getExitCode()
    {
        return exitCode;
    }

    public String getInitiator()
    {
        return initiator;
    }

    void updateExitCode()
    {
        if(process == null)
            LOGGER.warn("{} {}: No associated process", service, name);
        else if(!process.isAlive() && exitCode == null)
        {
            exitCode = process.exitValue();
            endTime = System.currentTimeMillis();
            LOGGER.info("{} {} exited with {}", service, name, exitCode);
        }
    }
    
    public String getService()
    {
        return service;
    }

    public String getName()
    {
        return name;
    }
    
    public boolean shouldKeepOnList()
    {
        if(process == null)
        {
            LOGGER.info("No process associated (yet)");
            return true/*um nicht zu früh zu enternt zu werden*/;
        }
        
        boolean alive = process.isAlive();
        LOGGER.info("{} {} alive: {}", service, name, alive);
        return alive;
    }
    
    public boolean isAlive()
    {
        return process != null ? process.isAlive() : false;
    }

    void destroyForcibly()
    {
        if(process != null)
            process.destroyForcibly();
    }

    public Path getWorkingDirectory()
    {
        return workingDirectory;
    }
    
    public List<String> getCmd()
    {
        return cmd;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public Long getEndTime()
    {
        return endTime;
    }
    
    public LocalDateTime getStartedAt()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
    }

    String getTitle()
    {
        return title;
    }

    public long getRuntime()
    {
        if(endTime != null)
            return endTime - startTime;
        else if(!isAlive())
            return -1;
        else
            return System.currentTimeMillis() - startTime;
    }
}
