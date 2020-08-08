package org.exolin.msp.system.lib;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tomgk
 */
public class Config
{
    public static Path getBaseDirectory()
    {
        return path("system.baseDirectory");
    }
    
    public static Path getLogDirectory()
    {
        return path("system.logDirectory");
    }
    
    public static Path getConfigDirectory()
    {
        return path("system.configDirectory");
    }

    private static Path path(String name)
    {
        String path = System.getProperty(name);
        
        if(path == null)
            throw new IllegalArgumentException(name+" was not given");
        
        return Paths.get(path);
    }
}
