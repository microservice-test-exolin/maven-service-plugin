package org.exolin.msp.service.linux;

import java.io.IOException;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.ApplicationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class SupervisordApplicationInstance implements ApplicationInstance
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisordApplicationInstance.class);
    private static final Log LOG = new Log()
    {
        @Override
        public void warn(String string)
        {
            LOGGER.warn(string);
        }

        @Override
        public void info(String string)
        {
            LOGGER.info(string);
        }
    };
    
    private final String name;

    public SupervisordApplicationInstance(String name)
    {
        this.name = name;
    }
    
    @Override
    public StatusInfo getStatus() throws IOException
    {
        return new SupervisordStatus(LinuxAbstraction.system2(LOG, "supervisorctl", "status", name));
    }
    
    static class SupervisordStatus implements StatusInfo
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
            throw new UnsupportedOperationException("Not supported yet.");
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
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getJavaOptions()
        {
            throw new UnsupportedOperationException("Not supported");
        }
    }
    
    private void supervisorctl(String command) throws IOException
    {
        String result = LinuxAbstraction.system2(LOG, "supervisorctl", command, name);
        if(result.contains("ERROR"))
        {
            LOGGER.error("Failed to {}:\n{}", command, result);
            throw new RuntimeException(result);
        }
    }

    @Override
    public void start() throws IOException
    {
        supervisorctl("start");
    }

    @Override
    public void stop() throws IOException
    {
        supervisorctl("stop");
    }

    @Override
    public void restart() throws IOException
    {
        supervisorctl("restart");
    }

    @Override
    public void setStartAtBoot(boolean b) throws IOException
    {
        //TODO
        throw new UnsupportedOperationException("Not supported yet");
    }
}
