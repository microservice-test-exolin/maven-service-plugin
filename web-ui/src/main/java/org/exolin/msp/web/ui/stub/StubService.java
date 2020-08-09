package org.exolin.msp.web.ui.stub;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.ProcessManager;
import org.exolin.msp.web.ui.Service;

/**
 *
 * @author tomgk
 */
public class StubService implements Service
{
    private final String name;
    private final SystemAbstraction sys;

    public StubService(String name, SystemAbstraction sys)
    {
        this.name = name;
        this.sys = sys;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public StatusInfo getStatus() throws IOException
    {
        return sys.getStatus(name);
    }

    @Override
    public void start() throws IOException
    {
        sys.start(name);
    }

    @Override
    public void stop() throws IOException
    {
        sys.stop(name);
    }

    @Override
    public void restart() throws IOException
    {
        sys.restart(name);
    }
    
    @Override
    public void build(ProcessManager pm) throws IOException, InterruptedException
    {
        
    }
    
    @Override
    public void deploy(ProcessManager pm) throws IOException, InterruptedException
    {
        
    }

    @Override
    public Map<String, Path> getLogFiles() throws IOException
    {
        return Collections.singletonMap("test.log", Paths.get("test.log"));
    }
}
