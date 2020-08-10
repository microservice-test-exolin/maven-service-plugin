package org.exolin.msp.web.ui.stub;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.AbstractService;
import org.exolin.msp.web.ui.ProcessManager;

/**
 *
 * @author tomgk
 */
public class StubService extends AbstractService
{
    public StubService(String name, SystemAbstraction sys)
    {
        super(name, sys);
    }

    @Override
    public boolean supportsBuildAndDeployment() throws IOException
    {
        return false;
    }
    
    @Override
    public void build(ProcessManager pm) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void deploy(ProcessManager pm) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Path> getLogFiles() throws IOException
    {
        return Collections.singletonMap("test.log", Paths.get("test.log"));
    }
}
