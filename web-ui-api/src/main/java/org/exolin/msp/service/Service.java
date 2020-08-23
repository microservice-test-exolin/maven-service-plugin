package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.core.StatusInfo;

/**
 *
 * @author tomgk
 */
public interface Service
{
    public String getName();
    public StatusInfo getStatus() throws IOException;
    
    public void start() throws IOException;
    public void stop() throws IOException;
    public void restart() throws IOException;
    
    public boolean supportsBuildAndDeployment() throws IOException;
    public void build(boolean async, String initiator) throws IOException, InterruptedException;
    public void deploy(boolean async, String initiator) throws IOException, InterruptedException;
    public boolean isBuildOrDeployProcessRunning();
    
    public Map<String, LogFile> getLogFiles(Optional<String> taskName) throws IOException;

    public Path getLocalGitRoot() throws IOException;
    public Path getLocalServiceMavenProject() throws IOException;
    public String getRepositoryUrl() throws IOException;
}
