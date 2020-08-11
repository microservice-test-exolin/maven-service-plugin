package org.exolin.msp.web.ui;

import java.io.IOException;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.SystemAbstraction;

/**
 *
 * @author tomgk
 */
public abstract class AbstractService implements Service
{
    private final String name;
    protected final SystemAbstraction sys;

    public AbstractService(String name, SystemAbstraction sys)
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
}
