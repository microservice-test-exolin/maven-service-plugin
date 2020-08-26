package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public class SimpleStatusInfo implements StatusInfo
{
    private final StatusType statusType;
    private final boolean startAtBootEnabled;

    public SimpleStatusInfo(StatusType statusType)
    {
        this(statusType, false);
    }
    
    public SimpleStatusInfo(StatusType statusType, boolean startAtBootEnabled)
    {
        this.statusType = statusType;
        this.startAtBootEnabled = startAtBootEnabled;
    }
    
    
    @Override
    public StatusType getStatus()
    {
        return statusType;
    }

    @Override
    public UnknowableBoolean isStartAtBootEnabled()
    {
        return UnknowableBoolean.of(startAtBootEnabled);
    }

    @Override
    public String getInfo()
    {
        return "";
    }
}
