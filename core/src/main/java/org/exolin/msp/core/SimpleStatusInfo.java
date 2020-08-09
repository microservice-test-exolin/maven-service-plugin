package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public class SimpleStatusInfo implements StatusInfo
{
    private boolean isRunning;

    public SimpleStatusInfo(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

    @Override
    public boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public String getInfo()
    {
        return "";
    }
}
