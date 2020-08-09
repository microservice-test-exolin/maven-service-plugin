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
    public boolean isRunning()
    {
        return parse(stdout);
    }

    @Override
    public String getInfo()
    {
        return stdout;
    }
    
    static boolean parse(String stdout)
    {
        if(stdout.contains("   Active: active"))
            return true;
        else if(stdout.contains("   Active: failed "))
            return false;
        else
            throw new UnsupportedOperationException("Can't parse:\n"+stdout);
    }
}
