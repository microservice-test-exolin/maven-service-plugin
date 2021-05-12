package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.linux.supervisord.SupervisordApplicationInstance;
import org.exolin.msp.service.linux.supervisord.SupervisordService;
import org.exolin.msp.service.pm.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxServices implements Services
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxServices.class);
    
    private final Path servicesDirectory;
    private final Path appDirectory;
    private final Path repoCollectionDirectory;
    private final ProcessManager pm;
    private final Map<String, LinuxService> serviceCache = new HashMap<>();

    public LinuxServices(Path servicesDirectory, Path appDirectory, Path repoCollectionDirectory, ProcessManager pm) throws IOException
    {
        this.servicesDirectory = servicesDirectory;
        this.appDirectory = appDirectory;
        this.repoCollectionDirectory = repoCollectionDirectory;
        this.pm = pm;
    }

    @Override
    public List<Service> getServices() throws IOException
    {
        return Collections.unmodifiableList(getLinuxServices());
    }
    
    public List<AbstractLinuxService> getLinuxServices() throws IOException
    {
        List<AbstractLinuxService> services = new ArrayList<>();
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(servicesDirectory))
        {
            for(Path p : dir)
                services.add(linuxService(p));
        }
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(appDirectory))
        {
            for(Path p : dir)
                services.add(supervisordService(p));
        }
        
        return services;
    }

    @Override
    public Service getService(String serviceName)
    {
        Path serviceDirectory = servicesDirectory.resolve(serviceName);
        if(!serviceDirectory.getParent().equals(servicesDirectory))
            throw new IllegalArgumentException("Invalid service name: "+serviceName);
        
        if(!Files.exists(serviceDirectory))
            return null;
        
        return linuxService(serviceDirectory);
    }
    
    private LinuxService linuxService(Path serviceDirectory)
    {
        return serviceCache.computeIfAbsent(serviceDirectory.getFileName().toString(), serviceName -> 
            new LinuxService(
                    serviceDirectory,
                    serviceDirectory.resolve("log"),
                    serviceDirectory.resolve("cfg"),
                    new LinuxServiceApplicationInstance(serviceName),
                    serviceName, pm)
        );
    }
    
    private SupervisordService supervisordService(Path appDirectory)
    {
        String serviceName = appDirectory.getFileName().toString();
        
        return new SupervisordService(
                serviceName,
                repoCollectionDirectory.resolve(serviceName),
                pm,
                new SupervisordApplicationInstance(serviceName)
        );
    }

    @Override
    public List<Service> getServicesFromRepositoryUrl(String url) throws IOException
    {
        LOGGER.info("getServiceFromRepositoryUrl({})", url);
        
        List<Service> services = new ArrayList<>();
        
        for(AbstractLinuxService s: getLinuxServices())
        {
            Optional<GitRepository> gitRepository = s.getGitRepository();
            if(!gitRepository.isPresent())
            {
                LOGGER.info("  {} => (no repository)", s.getName());
                continue;
            }
            
            String repoUrl = gitRepository.get().getRepositoryUrl();
            LOGGER.info("  {} => {}", s.getName(), repoUrl);
            
            //info: getRepositoryUrl() kann null zurück geben
            if(url.equals(repoUrl))
            {
                LOGGER.info("  found");
                services.add(s);
            }
        }
        
        if(services.isEmpty())
            LOGGER.info("  not found");
        
        return services;
    }
}
