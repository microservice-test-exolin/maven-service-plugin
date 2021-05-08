package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.service.ApplicationInstance;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.pm.ProcessManager;

/**
 *
 * @author tomgk
 */
public class SupervisordService extends AbstractLinuxService
{
    private final Path gitRoot;
    private final ApplicationInstance applicationInstance;
    
    public SupervisordService(String name, Path gitRoot, ProcessManager pm, ApplicationInstance applicationInstance)
    {
        super(name, pm);
        this.gitRoot = gitRoot;
        this.applicationInstance = applicationInstance;
    }

    @Override
    public ApplicationInstance getApplicationInstance()
    {
        return applicationInstance;
    }
    
    @Override
    public Map<String, LogFile> getServiceLogFiles() throws IOException
    {
        //TODO: read supervisord logs
        return Collections.emptyMap();
    }

    @Override
    public Optional<GitRepository> getGitRepository() throws IOException
    {
        return Optional.of(new DeployableGoGitRepository(getName(), gitRoot, this));
    }

    @Override
    public Iterable<String> getTasks()
    {
        return Collections.singleton("build+deploy");
    }

    @Override
    public List<String> getConfigFiles() throws IOException
    {
        return Collections.emptyList();
    }

    @Override
    public ConfigFile getConfigFile(String name) throws IOException
    {
        throw new NoSuchFileException(name);
    }
}
