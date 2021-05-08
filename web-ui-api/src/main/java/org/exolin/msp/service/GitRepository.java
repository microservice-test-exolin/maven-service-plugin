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
    
    public boolean supportsBuildAndDeployment() throws IOException;
    public void build(boolean async, String initiator) throws IOException, InterruptedException;
    public void deploy(boolean async, String initiator) throws IOException, InterruptedException;
    public boolean isBuildOrDeployProcessRunning();
}
