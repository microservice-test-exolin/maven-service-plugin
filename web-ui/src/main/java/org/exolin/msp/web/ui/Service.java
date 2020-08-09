package org.exolin.msp.web.ui;

import java.io.IOException;
import java.util.List;
import org.exolin.msp.core.StatusInfo;

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
    
    public void build(List<String> log) throws IOException, InterruptedException;
    public void deploy(List<String> log) throws IOException, InterruptedException;
}
