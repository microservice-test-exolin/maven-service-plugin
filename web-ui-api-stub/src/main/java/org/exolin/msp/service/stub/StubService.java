package org.exolin.msp.service.stub;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.AbstractService;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.LogFile;

/**
 *
 * @author tomgk
 */
public class StubService extends AbstractService
{
    private final GitRepository gitRepository;
    private final Map<String, LogFile> logFiles;
    
    public StubService(String name, Path gitRootPath, Path localServiceMavenProject, String repositoryUrl, SystemAbstraction sys, Map<String, LogFile> logFiles)
    {
        super(name, sys);
        
        this.gitRepository = new StubGitRepository(this, gitRootPath, localServiceMavenProject, repositoryUrl);
        this.logFiles = logFiles;
    }
    
    private long processStart;
    private static final long PROCESS_DURATION = 3000;  //3 s simulierter Lauf
    
    static final String BUILD = "build";
    static final String DEPLOY = "deploy";
    
    @Override
    public Iterable<String> getTasks()
    {
        return Arrays.asList(BUILD, DEPLOY);
    }

    boolean isBuildOrDeployProcessRunning()
    {
        if(processStart == 0)
            return false;
        
        return System.currentTimeMillis() - processStart < PROCESS_DURATION;
    }
    
    void startProcess(boolean asynch) throws InterruptedException
    {
        processStart = System.currentTimeMillis();
        
        if(!asynch)
            Thread.sleep(PROCESS_DURATION);
    }

    @Override
    public Optional<GitRepository> getGitRepository() throws IOException
    {
        return Optional.of(gitRepository);
    }

    @Override
    public Map<String, LogFile> getServiceLogFiles() throws IOException
    {
        return getLogFiles(Optional.empty());
    }

    @Override
    public Map<String, LogFile> getTaskLogFiles(String taskName) throws IOException
    {
        return getLogFiles(Optional.of(taskName));
    }
    
    private Map<String, LogFile> getLogFiles(Optional<String> taskName) throws IOException
    {
        Map<String, LogFile> filtered = new HashMap<>(logFiles);
        filtered.values().removeIf(l -> !l.getProcessName().equals(taskName));
        return filtered;
    }

    @Override
    public List<String> getConfigFiles() throws IOException
    {
        return Arrays.asList(
                "test.config",
                "database.properties",
                "bot.properties"
        );
    }
    
    private final InMemoryConfigFile testConfig = new InMemoryConfigFile(new HashMap<>(Collections.singletonMap("key", "value")));

    @Override
    public ConfigFile getConfigFile(String name) throws IOException
    {
        if(name.equals("test.config"))
            return testConfig;
        else
            throw new NoSuchFileException(name);
    }
}
