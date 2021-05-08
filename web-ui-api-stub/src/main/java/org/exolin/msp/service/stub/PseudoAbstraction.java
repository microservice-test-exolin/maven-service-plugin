package org.exolin.msp.service.stub;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.ApplicationInstance;

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
    public ApplicationInstance getNativeService(String name)
    {
        return new StubApplicationInstance(name);
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

}
