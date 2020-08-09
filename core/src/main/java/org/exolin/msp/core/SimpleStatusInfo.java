package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public class SimpleStatusInfo implements StatusInfo
{
    private final boolean isRunning;

    public SimpleStatusInfo(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

    @Override
    public StatusType getStatus()
    {
        return isRunning ? StatusType.ACTIVE : StatusType.INACTIVE;
    }

    @Override
    public String getInfo()
    {
        return "";
    }
}
