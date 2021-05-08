package org.exolin.msp.service.stub;

import java.io.IOException;
import org.exolin.msp.core.SimpleStatusInfo;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.ApplicationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class StubApplicationInstance implements ApplicationInstance
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StubApplicationInstance.class);
    private StatusType status = StatusType.ACTIVE;
    private boolean enabled = true;
    
    private final String name;

    public StubApplicationInstance(String name)
    {
        this.name = name;
    }
    
    @Override
    public void restart() throws IOException
    {
        LOGGER.warn("Pseudo > Restart "+name);
        status = StatusType.ACTIVE;
    }

    @Override
    public StatusInfo getStatus() throws IOException
    {
        LOGGER.warn("Pseudo > Check status for "+name);
        return new SimpleStatusInfo(status, enabled);
    }

    @Override
    public void start() throws IOException
    {
        LOGGER.warn("Pseudo > Start "+name);
        status = StatusType.ACTIVE;
    }

    @Override
    public void stop() throws IOException
    {
        LOGGER.warn("Pseudo > Stop "+name);
        status = StatusType.INACTIVE;
    }

    @Override
    public void setStartAtBoot(boolean b) throws IOException
    {
        enabled = b;
    }
    
    public void setFailed() throws IOException
    {
        status = StatusType.FAILED;
    }
}
