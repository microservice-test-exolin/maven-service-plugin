package org.exolin.msp.web.ui.stub;

import org.exolin.msp.web.ui.Service;

/**
 *
 * @author tomgk
 */
public class StubService implements Service
{
    private final String name;
    private String status;

    public StubService(String name)
    {
        this.name = name;
        this.status = "Running";
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getStatus()
    {
        return status;
    }

    @Override
    public void start()
    {
        status = "Running";
    }

    @Override
    public void stop()
    {
        status = "Stopped";
    }

    @Override
    public void restart()
    {
        status = "Running";
    }
}
