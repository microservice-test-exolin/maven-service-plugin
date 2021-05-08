package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Go application that's deployable to supervisord
 * 
 * @author tomgk
 */
public class DeployableGoGitRepository extends AbstractGitRepository
{
    private final Path gitRoot;
    private final LinuxService linuxService;

    public DeployableGoGitRepository(Path gitRoot, LinuxService linuxService)
    {
        this.gitRoot = gitRoot;
        this.linuxService = linuxService;
    }

    @Override
    public Path getLocalGitRoot() throws IOException
    {
        return gitRoot;
    }

    @Override
    public Path getLocalServiceMavenProject() throws IOException
    {
        return gitRoot;
    }

    @Override
    public boolean supports(Task task) throws IOException
    {
        return task == Task.BUILD_AND_DEPLOY;
    }

    @Override
    public void run(Task task, boolean async, String initiator) throws IOException, InterruptedException
    {
        if(task == Task.BUILD_AND_DEPLOY)
            buildAndDeploy(async, initiator);
        else
            throw new UnsupportedOperationException();
    }

    private void buildAndDeploy(boolean async, String initiator) throws IOException, InterruptedException
    {
        Path serviceSrcDirectory = getLocalServiceMavenProject();
        
        String[] cmd = {"/bin/bash", "-c", "/root/repos/go-deploy/setup.sh -s /root/repos/go-e621-tg-bot -o /root/apps/go-e621-tg-bot/go-e621-tg-bot -a go-e621-tg-bot -f"};
        
        linuxService.start(serviceSrcDirectory, LinuxService.TASK_DEPLOY, cmd, async, initiator);
    }

    @Override
    public boolean isTaskRunning()
    {
        return linuxService.isTaskRunning();
    }

    @Override
    public String toString()
    {
        return getClass().getName()+"[localRepo="+gitRoot+"]";
    }
}
