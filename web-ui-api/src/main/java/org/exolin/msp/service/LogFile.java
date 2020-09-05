package org.exolin.msp.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 *
 * @author tomgk
 */
public abstract class LogFile
{
    private final String serviceName;
    private final Optional<String> processName;

    public LogFile(String serviceName, Optional<String> processName)
    {
        this.serviceName = serviceName;
        this.processName = processName;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public Optional<String> getProcessName()
    {
        return processName;
    }
    
    public abstract String getFileName();
    public abstract String getTitle();
    
    public abstract void writeTo(OutputStream out) throws IOException;

    /*public Path getPath()
    {
        return path;
    }*/
}
