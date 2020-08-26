package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public class LinuxStatusInfo implements StatusInfo
{
    private final String stdout;

    public LinuxStatusInfo(String stdout)
    {
        this.stdout = stdout;
    }
    
    @Override
    public StatusType getStatus()
    {
        return parse(stdout);
    }

    @Override
    public UnknowableBoolean isStartAtBootEnabled()
    {
        if(stdout.contains("; enabled;"))
            return UnknowableBoolean.TRUE;
        else if(stdout.contains("; disabled;"))
            return UnknowableBoolean.FALSE;
        else
            return UnknowableBoolean.UNKNOWN;
    }

    @Override
    public String getInfo()
    {
        return stdout;
    }
    
    static StatusType parse(String stdout)
    {
        if(stdout.contains("   Active: active"))
            return StatusType.ACTIVE;
        else if(stdout.contains("   Active: failed "))
            return StatusType.FAILED;
        else if(stdout.contains("   Active: inactive "))
            return StatusType.INACTIVE;
        else
            return StatusType.UNKNOWN;
    }
}
