package org.exolin.msp.service.linux.supervisord;

import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;

/**
 *
 * @author tomgk
 */
public class SupervisordStatus implements StatusInfo
{
    private final String output;

    public SupervisordStatus(String output)
    {
        this.output = output;
    }

    @Override
    public StatusType getStatus()
    {
        //TODO: better implementation
        if(output.contains(" RUNNING "))
            return StatusType.ACTIVE;
        else if(output.contains(" STOPPED "))
            return StatusType.INACTIVE;
        //TODO: validate
        else if(output.contains(" FAILED "))
            return StatusType.FAILED;
        else
            return StatusType.UNKNOWN;
    }

    @Override
    public String getInfo()
    {
        return output;
    }

    @Override
    public UnknowableBoolean isStartAtBootEnabled()
    {
        return UnknowableBoolean.UNKNOWN;
    }

    @Override
    public String getMemory()
    {
        return null;//TODO throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Long getJavaPID()
    {
        String needle = " pid ";

        int p = output.indexOf(needle);
        if(p == -1)
            return null;

        p += needle.length();

        int p2 = output.indexOf(',', p);
        if(p2 == -1)
            return null;

        return Long.parseLong(output.substring(p, p2));
    }

    @Override
    public String getJavaCMD()
    {
        return "";//TODO throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getJavaOptions()
    {
        return "";//TODO throw new UnsupportedOperationException("Not supported");
    }
}
