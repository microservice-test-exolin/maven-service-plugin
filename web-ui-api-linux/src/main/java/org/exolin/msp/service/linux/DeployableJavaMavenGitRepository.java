package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A git repository that contains a maven java project that can be deployed using /root/repos/deploy.sh.
 * The build happens with the root project in the repository,
 * the deploy happens with a specific project in the repository which can be
 * the root project or a (sub)module of the root.
 *
 * @author tomgk
 */
public class DeployableJavaMavenGitRepository extends AbstractGitRepository
{
    private final LinuxService linuxService;
    private final Path localServiceMavenProject;

    public DeployableJavaMavenGitRepository(LinuxService linuxService, Path localServiceMavenProject)
    {
        this.linuxService = linuxService;
        this.localServiceMavenProject = localServiceMavenProject;
    }

    @Override
    public boolean isTaskRunning()
    {
        return linuxService.isTaskRunning();
    }

    @Override
    public Path getLocalGitRoot() throws IOException
    {
        return getGitRoot(getLocalServiceMavenProject());
    }
    
    @Override
    public Path getLocalServiceMavenProject() throws IOException
    {
        return localServiceMavenProject;
    }

    @Override
    public boolean supports(Task task) throws IOException
    {
        return task == Task.BUILD || task == Task.DEPLOY;
        /*try{
            getGitRepository().getLocalServiceMavenProject();
            return true;
        }catch(UnsupportedOperationException e){
            LOGGER.info("Couldn't determine original path", e);
            return false;
        }*/
    }

    @Override
    public void run(Task task, boolean async, String initiator) throws IOException, InterruptedException
    {
        switch(task)
        {
            case BUILD:
                build(async, initiator);
                break;
                
            case DEPLOY:
                deploy(async, initiator);
                break;
                
            default:
                throw new UnsupportedOperationException(task.toString());
        }
    }
    
    private void build(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path gitRoot = getLocalGitRoot();
        
        String[] cmd = {"/bin/bash", "-c", "git pull && mvn package"};
        
        linuxService.start(gitRoot, LinuxService.TASK_BUILD, cmd, asynch, initiator);
    }
    
    private void deploy(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path serviceSrcDirectory = getLocalServiceMavenProject();
        
        String[] cmd = Deployer.getDeployer().deployMaven();
        
        linuxService.start(serviceSrcDirectory, LinuxService.TASK_DEPLOY, cmd, asynch, initiator);
    }

    @Override
    public String toString()
    {
        return getClass().getName()+"[localServiceMavenProject="+localServiceMavenProject+"]";
    }
}
