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
    public boolean isStartAtBootEnabled()
    {
        if(stdout.contains("; enabled;"))
            return true;
        else if(stdout.contains("; disabled;"))
            return false;
        else
            throw new UnsupportedOperationException("can't determine if enabled from "+stdout);
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
