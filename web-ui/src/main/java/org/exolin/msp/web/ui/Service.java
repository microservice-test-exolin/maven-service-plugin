package org.exolin.msp.web.ui;

import java.io.IOException;

/**
 *
 * @author tomgk
 */
public interface Service
{
    public String getName();
    public String getStatus();
    
    public void start() throws IOException;
    public void stop() throws IOException;
    public void restart() throws IOException;
}
