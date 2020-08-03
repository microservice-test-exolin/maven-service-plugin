package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author tomgk
 */
public class PseudoAbstraction implements SystemAbstraction
{
    private final Log log;

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
    }

    @Override
    public void getStatus(String name) throws IOException
    {
        log.warn("Pseudo > Check status for "+name);
    }
}
