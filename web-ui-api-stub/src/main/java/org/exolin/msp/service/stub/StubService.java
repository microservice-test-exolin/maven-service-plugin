package org.exolin.msp.service.stub;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.AbstractService;
import org.exolin.msp.service.LogFile;

/**
 *
 * @author tomgk
 */
public class StubService extends AbstractService
{
    private final Map<String, LogFile> logFiles;
    
    public StubService(String name, SystemAbstraction sys, Map<String, LogFile> logFiles)
    {
        super(name, sys);
        this.logFiles = logFiles;
    }

    @Override
    public boolean supportsBuildAndDeployment() throws IOException
    {
        return false;
    }
    
    @Override
    public void build(boolean asynch) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void deploy(boolean asynch) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBuildOrDeployProcessRunning()
    {
        return false;
    }
    
    @Override
    public String getRepositoryUrl() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, LogFile> getLogFiles(Optional<String> taskName) throws IOException
    {
        if(taskName == null)
            return logFiles;
        
        Map<String, LogFile> filtered = new HashMap<>(logFiles);
        filtered.values().removeIf(l -> !l.getProcessName().equals(taskName));
        return filtered;
    }
}
