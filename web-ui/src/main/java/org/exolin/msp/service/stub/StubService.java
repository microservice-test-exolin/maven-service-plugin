package org.exolin.msp.service.stub;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.AbstractService;

/**
 *
 * @author tomgk
 */
public class StubService extends AbstractService
{
    private final Map<String, Path> logFiles;
    
    public StubService(String name, SystemAbstraction sys, Map<String, Path> logFiles)
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
    public String getRepositoryUrl() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Path> getLogFiles() throws IOException
    {
        return logFiles;
    }
}
