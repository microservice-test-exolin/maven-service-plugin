package org.exolin.msp.service.linux;

import java.util.Optional;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;

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
        return get(stdout, "Memory");
        
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
    
    static String get(String stdout, String name)
    {
        String prefix = name+": ";
        int i = stdout.indexOf(prefix);
        if(i == -1)
            return null;
        
        int el = stdout.indexOf('\n', i);
        if(el == -1)
            el = stdout.length();
        
        return stdout.substring(i + prefix.length(), el);
    }
    
    public Long getMainProcessId()
    {
        return getMainProcessId(stdout);
    }

    static Long getMainProcessId(String stdout)
    {
        return Optional.ofNullable(get(stdout, "Main PID")).map(Long::parseLong).orElse(null);
    }
    
    static String getLine(String stdout)
    {
        int i = stdout.indexOf("└─");
        if(i == -1)
            return null;
        
        int el = stdout.indexOf('\n', i);
        if(el == -1)
            el = stdout.length();
        
        return stdout.substring(i + 2, el);
    }
    
    @Override
    public Long getJavaPID()
    {
        return getJavaPID(stdout);
    }
    
    static Long getJavaPID(String stdout)
    {
        String line = getLine(stdout);
        if(line == null)
            return null;
        
        int i = line.indexOf(' ');
        return Long.parseLong(line.substring(0, i));
    }
    
    @Override
    public String getJavaCMD()
    {
        return getJavaCMD(stdout);
    }
    
    static String getJavaCMD(String stdout)
    {
        String line = getLine(stdout);
        if(line == null)
            return null;
        
        int i = line.indexOf(' ');
        return line.substring(i+1);
    }
    
    @Override
    public String getJavaOptions()
    {
        return getJavaOptions(stdout);
    }
    
    static String getJavaOptions(String stdout)
    {
        String cmd = getJavaCMD(stdout);
        if(cmd == null)
            return null;
        
        return getJavaOptionsFromJavaCMD(cmd);
    }
    
    static String getJavaOptionsFromJavaCMD(String cmd)
    {
        int a = cmd.indexOf(' ');
        int b = cmd.indexOf("-jar");
        if(a == -1 || b == -1 || a > b)
            return null;
        
        // "java -jar" [...] => keine Optionen
        if(a+1==b)
            return "";
        
        return cmd.substring(a+1, b-1);
    }
}
