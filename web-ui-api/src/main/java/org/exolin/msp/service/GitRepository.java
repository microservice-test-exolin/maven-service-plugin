package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author tomgk
 */
public interface GitRepository
{
    public Path getLocalGitRoot() throws IOException;
    public Path getLocalServiceMavenProject() throws IOException;
    public String getRepositoryUrl() throws IOException;
    
    enum Task
    {
        BUILD,
        DEPLOY,
        BUILD_AND_DEPLOY
    }
    
    public boolean supports(Task task) throws IOException;
    public void run(Task task, boolean async, String initiator) throws IOException, InterruptedException;
    public boolean isTaskRunning();
}
