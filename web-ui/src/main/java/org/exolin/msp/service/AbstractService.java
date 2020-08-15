package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.SystemAbstraction;

/**
 *
 * @author tomgk
 */
public abstract class AbstractService implements Service
{
    private final String name;
    protected final SystemAbstraction sys;
    private final Path logDirectory;

    public AbstractService(String name, SystemAbstraction sys, Path logDirectory)
    {
        this.name = name;
        this.sys = sys;
        this.logDirectory = logDirectory;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public StatusInfo getStatus() throws IOException
    {
        return sys.getStatus(name);
    }

    @Override
    public void start() throws IOException
    {
        sys.start(name);
    }

    @Override
    public void stop() throws IOException
    {
        sys.stop(name);
    }

    @Override
    public void restart() throws IOException
    {
        sys.restart(name);
    }
    
    protected Path getLogDirectory()
    {
        return logDirectory;
    }
    
    @Override
    public final Map<String, Path> getLogFiles() throws IOException
    {
        Map<String, Path> files = new TreeMap<>();
        
        //LOGGER.info("Reading log file list for {} from {}", getName(), logDir);
        
        for(Path p: Files.newDirectoryStream(getLogDirectory()))
        {
            //LOGGER.info("- {}", p.getFileName());
            files.put(p.getFileName().toString(), p);
        }
        
        return files;
    }
}
