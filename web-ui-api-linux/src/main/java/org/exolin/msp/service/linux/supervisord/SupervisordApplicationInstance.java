package org.exolin.msp.service.linux.supervisord;

import java.io.IOException;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.service.ApplicationInstance;
import org.exolin.msp.service.linux.LinuxAbstraction;
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
