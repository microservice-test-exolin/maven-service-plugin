package org.exolin.msp.web.ui;

/**
 *
 * @author tomgk
 */
public interface Service
{
    public String getName();
    public String getStatus();
    
    public void start();
    public void stop();
    public void restart();
}
