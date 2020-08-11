package org.exolin.msp.web.ui.linux;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;

/**
 *
 * @author tomgk
 */
public class LinuxServices implements Services
{
    private final Path servicesDirectory;
    private final SystemAbstraction sys;

    public LinuxServices(Path servicesDirectory, SystemAbstraction sys)
    {
        this.servicesDirectory = servicesDirectory;
        this.sys = sys;
    }

    @Override
    public List<Service> getServices() throws IOException
    {
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(servicesDirectory))
        {
            List<Service> services = new ArrayList<>();
            
            for(Path p : dir)
                services.add(new LinuxService(p, p.getFileName().toString(), sys));
            
            return services;
        }
    }

    @Override
    public Service getService(String serviceName)
    {
        Path sDir = servicesDirectory.resolve(serviceName);
        if(!sDir.getParent().equals(servicesDirectory))
            throw new IllegalArgumentException("Invalid service name: "+serviceName);
        
        if(!Files.exists(sDir))
            return null;
        
        return new LinuxService(sDir, serviceName, sys);
    }
}
