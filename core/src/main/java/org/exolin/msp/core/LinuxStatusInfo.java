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
    
    @Override
    public String getMemory()
    {
        return getMemory(stdout);
    }
    
    static String getMemory(String stdout)
    {
        String prefix = "Memory: ";
        int i = stdout.indexOf(prefix);
        if(i == -1)
            return null;
        
        int el = stdout.indexOf('\n', i);
        if(el == -1)
            el = stdout.length();
        
        String mem = stdout.substring(i + prefix.length(), el);
        return mem;
        /*
        double fac = Double.parseDouble(mem.substring(0, mem.length()-1));
        long unit;
        switch(mem.charAt(mem.length()-1))
        {
            case 'k': unit = 1024; break;
            case 'M': unit = 1024 * 1024; break;
            case 'G': unit = 1024 * 1024 * 1024; break;
            default: throw new IllegalArgumentException(mem);
        }
        
        return (long)(fac * unit);*/
    }
}
