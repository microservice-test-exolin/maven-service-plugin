package org.exolin.msp.web.ui.stub;

import java.io.IOException;
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
    public String getStatus()
    {
        try{
            return sys.isRunning(name) ? "Running" : "Stopped";
        }catch(IOException ex){
            return "Couldn't be determined ("+ex+")";
        }
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
}
