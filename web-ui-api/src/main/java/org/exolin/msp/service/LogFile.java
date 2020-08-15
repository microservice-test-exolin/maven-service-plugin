package org.exolin.msp.service;

import java.nio.file.Path;
import java.util.Optional;

/**
 *
 * @author tomgk
 */
public class LogFile
{
    private final String serviceName;
    private final Optional<String> processName;
    private final Path path;

    public LogFile(String serviceName, Optional<String> processName, Path path)
    {
        this.serviceName = serviceName;
        this.processName = processName;
        this.path = path;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public Optional<String> getProcessName()
    {
        return processName;
    }

    public Path getPath()
    {
        return path;
    }
}
