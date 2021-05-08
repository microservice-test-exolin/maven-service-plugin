package org.exolin.msp.service.linux;

import java.io.IOException;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.service.ApplicationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxServiceApplicationInstance implements ApplicationInstance
{
    private final String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxServiceApplicationInstance.class);
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

    public LinuxServiceApplicationInstance(String name)
    {
        this.name = name;
    }

    @Override
    public void start() throws IOException
    {
        LinuxAbstraction.system(LOG, "systemctl", "start", name);
    }

    @Override
    public void stop() throws IOException
    {
        LinuxAbstraction.system(LOG, "systemctl", "stop", name);
    }

    @Override
    public void restart() throws IOException
    {
        LinuxAbstraction.system(LOG, "systemctl", "restart", name);
    }

    @Override
    public void setStartAtBoot(boolean b) throws IOException
    {
        LinuxAbstraction.system(LOG, "systemctl", b ? "enable" : "disable", name);
    }
    
    @Override
    public StatusInfo getStatus() throws IOException
    {
        return new LinuxStatusInfo(LinuxAbstraction.system2(LOG, "systemctl", "status", name));
    }
}
