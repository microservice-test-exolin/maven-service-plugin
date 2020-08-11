package org.exolin.msp.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tomgk
 */
public class PseudoAbstraction implements SystemAbstraction
{
    private final Log log;
    private final Set<String> running = new HashSet<>();

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
        running.add(name);
    }

    @Override
    public StatusInfo getStatus(String name) throws IOException
    {
        log.warn("Pseudo > Check status for "+name);
        return new SimpleStatusInfo(running.contains(name));
    }

    @Override
    public void start(String name) throws IOException
    {
        log.warn("Pseudo > Start "+name);
        running.add(name);
    }

    @Override
    public void stop(String name) throws IOException
    {
        log.warn("Pseudo > Stop "+name);
        running.remove(name);
    }

    @Override
    public String getGitRepositoryUrl(Path originalPath) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
