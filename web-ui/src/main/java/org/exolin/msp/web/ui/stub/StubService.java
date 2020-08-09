package org.exolin.msp.web.ui.stub;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.SystemAbstraction;
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
    
    public void build(List<String> log) throws IOException, InterruptedException
    {
        log.add("Not supported");
    }
}
