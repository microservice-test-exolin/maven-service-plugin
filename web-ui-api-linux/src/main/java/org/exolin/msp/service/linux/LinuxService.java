package org.exolin.msp.service.linux;

import org.exolin.msp.service.linux.config.TokenConfigFile;
import org.exolin.msp.service.linux.config.PropertiesConfigFile;
import org.exolin.msp.service.log.ProcessCallLogFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.exolin.msp.service.ApplicationInstance;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.pm.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxService extends AbstractLinuxService
{
    private final Path serviceDirectory;
    private final Path logDirectory;
    private final Path configDirectory;
    private final ApplicationInstance applicationInstance;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxService.class);
    
    public LinuxService(
            Path serviceDirectory,
            Path logDirectory,
            Path configDirectory,
            ApplicationInstance applicationInstance,
            String name,
            ProcessManager pm)
    {
        super(name, pm);
        this.serviceDirectory = serviceDirectory;
        this.logDirectory = logDirectory;
        this.configDirectory = configDirectory;
        this.applicationInstance = applicationInstance;
    }

    @Override
    public ApplicationInstance getApplicationInstance()
    {
        return applicationInstance;
    }
    
    @Override
    public Optional<GitRepository> getGitRepository() throws IOException
    {
        Path originalPathFile = serviceDirectory.resolve("original.path");
        Path localServiceMavenProject;
        try{
            localServiceMavenProject = Paths.get(new String(Files.readAllBytes(originalPathFile), StandardCharsets.UTF_8));
        }catch(NoSuchFileException e){
            LOGGER.warn("Can't determine original path of "+getName(), e);
            return Optional.empty();
        }
        
        return Optional.of(new DeployableJavaMavenGitRepository(this, localServiceMavenProject));
    }

    public static String getLogicalLogFileName(String processName, long timestamp)
    {
        return "task/"+processName+"/"+timestamp+".log";
    }
    
    public static ProcessCallLogFile Journalctl(String serviceName)
    {
        return new ProcessCallLogFile(serviceName, "journalctl", "Standard Output", new String[]{"journalctl", "-u", serviceName});
    }

    @Override
    public Map<String, LogFile> getServiceLogFiles() throws IOException
    {
        Map<String, LogFile> files = new TreeMap<>();
        
        files.put("journalctl", Journalctl(getName()));

        readLogFiles(Optional.empty(), logDirectory, files);
        
        return files;
    }
    
    static final String TASK_BUILD = "build";
    static final String TASK_DEPLOY = "deploy";

    @Override
    public Iterable<String> getTasks()
    {
        return Arrays.asList(TASK_BUILD, TASK_DEPLOY);
    }
    
    @Override
    public List<String> getConfigFiles() throws IOException
    {
        try{
            return Files.list(configDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.toList());
        }catch(NoSuchFileException e){
            return Collections.emptyList();
        }
    }

    @Override
    public ConfigFile getConfigFile(String name) throws IOException
    {
        Path config = configDirectory.resolve(name);
        if(!config.normalize().startsWith(configDirectory))
            throw new IllegalArgumentException("Not in config directory: "+name);
        
        if(name.endsWith(".token"))
            return new TokenConfigFile(config, new String(Files.readAllBytes(config), StandardCharsets.UTF_8));
        else
        {
            Properties properties = new Properties();
            try(BufferedReader in = Files.newBufferedReader(config))
            {
                properties.load(in);
            }

            return new PropertiesConfigFile(config, properties);
        }
    }
}
