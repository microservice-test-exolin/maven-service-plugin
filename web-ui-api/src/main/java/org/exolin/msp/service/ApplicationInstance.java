package org.exolin.msp.service;

import java.io.IOException;
import org.exolin.msp.core.StatusInfo;

/**
 *
 * @author tomgk
 */
public interface ApplicationInstance
{
    public StatusInfo getStatus() throws IOException;
    
    public void start() throws IOException;
    public void stop() throws IOException;
    public void restart() throws IOException;
    public void setStartAtBoot(boolean b) throws IOException;
}
