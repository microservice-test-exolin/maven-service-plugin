package org.exolin.msp.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tomgk
 */
public class PseudoAbstraction implements SystemAbstraction
{
    private final Log log;
    private final Map<String, StatusType> running = new HashMap<>();

    public PseudoAbstraction(Log log)
    {
        this.log = log;
    }
    
    @Override
    public void setOwner(Path serviceDir, String serviceUser) throws IOException
    {
        log.warn("Pseudo > chown -R "+serviceDir+" "+serviceUser);
    }

    @Override
    public void reloadDeamon() throws IOException
    {
        log.warn("Pseudo > Reload deamon");
    }

    @Override
    public void restart(String name) throws IOException
    {
        log.warn("Pseudo > Restart "+name);
        running.put(name, StatusType.ACTIVE);
    }

    @Override
    public StatusInfo getStatus(String name) throws IOException
    {
        log.warn("Pseudo > Check status for "+name);
        return new SimpleStatusInfo(running.getOrDefault(name, StatusType.INACTIVE));
    }

    @Override
    public void start(String name) throws IOException
    {
        log.warn("Pseudo > Start "+name);
        running.put(name, StatusType.ACTIVE);
    }

    @Override
    public void stop(String name) throws IOException
    {
        log.warn("Pseudo > Stop "+name);
        running.remove(name);
    }
    
    public void setFailed(String name) throws IOException
    {
        running.put(name, StatusType.FAILED);
    }
}
