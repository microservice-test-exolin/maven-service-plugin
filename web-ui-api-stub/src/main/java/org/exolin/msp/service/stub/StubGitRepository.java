package org.exolin.msp.service.stub;

import java.io.IOException;
import java.nio.file.Path;
import org.exolin.msp.service.GitRepository;

/**
 *
 * @author tomgk
 */
public class StubGitRepository implements GitRepository
{
    private final StubService stubService;
    private final Path localGitRootPath;
    private final Path localServiceMavenProject;
    private final String repositoryUrl;

    public StubGitRepository(StubService stubService, Path localGitRootPath, Path localServiceMavenProject, String repositoryUrl)
    {
        this.stubService = stubService;
        
        if(!localServiceMavenProject.startsWith(localGitRootPath))
            throw new IllegalArgumentException(localServiceMavenProject+" is not in "+localGitRootPath);
        
        this.localGitRootPath = localGitRootPath;
        this.localServiceMavenProject = localServiceMavenProject;
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public boolean supports(Task task) throws IOException
    {
        return true;
    }

    @Override
    public boolean isTaskRunning()
    {
        return stubService.isBuildOrDeployProcessRunning();
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
    public void run(Task task, boolean asynch, String initiator) throws IOException, InterruptedException
    {
        stubService.startProcess(asynch);
    }
}
