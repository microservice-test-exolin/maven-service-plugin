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
            throw new UnsupportedOperationException("Can't parse:\n"+stdout);
    }
}
