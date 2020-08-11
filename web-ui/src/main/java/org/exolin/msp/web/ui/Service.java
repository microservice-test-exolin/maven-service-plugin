package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.web.ui.pm.ProcessManager;

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
    public void build(ProcessManager pm) throws IOException, InterruptedException;
    public void deploy(ProcessManager pm) throws IOException, InterruptedException;
    
    public Map<String, Path> getLogFiles() throws IOException;
}
