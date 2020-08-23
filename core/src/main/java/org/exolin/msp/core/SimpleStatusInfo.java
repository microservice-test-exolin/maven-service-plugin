package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public class SimpleStatusInfo implements StatusInfo
{
    private final StatusType statusType;

    public SimpleStatusInfo(StatusType statusType)
    {
        this.statusType = statusType;
    }

    @Override
    public StatusType getStatus()
    {
        return statusType;
    }

    @Override
    public String getInfo()
    {
        return "";
    }
}
