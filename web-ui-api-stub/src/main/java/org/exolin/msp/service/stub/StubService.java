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
    private final Path localGitRootPath;
    private final Path localServiceMavenProject;
    private final String repositoryUrl;
    private final Map<String, LogFile> logFiles;
    
    public StubService(String name, Path gitRootPath, Path localServiceMavenProject, String repositoryUrl, SystemAbstraction sys, Map<String, LogFile> logFiles)
    {
        super(name, sys);
        
        if(!localServiceMavenProject.startsWith(gitRootPath))
            throw new IllegalArgumentException(localServiceMavenProject+" is not in "+gitRootPath);
        
        this.localGitRootPath = gitRootPath;
        this.localServiceMavenProject = localServiceMavenProject;
        this.repositoryUrl = repositoryUrl;
        this.logFiles = logFiles;
    }
    
    @Override
    public boolean supportsBuildAndDeployment() throws IOException
    {
        return true;
    }
    
    private long processStart;
    
    @Override
    public void build(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        processStart = System.currentTimeMillis();
    }
    
    @Override
    public void deploy(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        processStart = System.currentTimeMillis();
    }

    @Override
    public boolean isBuildOrDeployProcessRunning()
    {
        if(processStart == 0)
            return false;
        
        return System.currentTimeMillis() - processStart < 3000;  //3 s simulierter Lauf
    }

    @Override
    public Path getLocalServiceMavenProject()
    {
        return localServiceMavenProject;
    }
    
    @Override
    public Path getLocalGitRoot() throws IOException
    {
        return localGitRootPath;
    }
    
    @Override
    public String getRepositoryUrl() throws IOException
    {
        return repositoryUrl;
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
