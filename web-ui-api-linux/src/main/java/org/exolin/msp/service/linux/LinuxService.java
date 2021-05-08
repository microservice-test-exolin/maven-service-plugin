package org.exolin.msp.service.linux;

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
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.AbstractService;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.pm.BuildOrDeployAlreadyRunningException;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxService extends AbstractService
{
    private final Path serviceDirectory;
    private final Path logDirectory;
    private final Path configDirectory;
    
    private final ProcessManager pm;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxService.class);
    
    private Process runningTaskProcess;
    
    public LinuxService(
            Path serviceDirectory,
            Path logDirectory,
            Path configDirectory,
            String name,
            SystemAbstraction sys,
            ProcessManager pm)
    {
        super(name, sys);
        this.serviceDirectory = serviceDirectory;
        this.logDirectory = logDirectory;
        this.configDirectory = configDirectory;
        this.pm = pm;
    }

    boolean isTaskRunning()
    {
        return runningTaskProcess != null && runningTaskProcess.isAlive();
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

    private void readLogFiles(Optional<String> processName, Path dir, Map<String, LogFile> files) throws IOException
    {
        String prefix = processName.map(s -> "task/"+s+"/").orElse("service/");
        
        try{
            for(Path p: Files.newDirectoryStream(dir, "*.log"))
                files.put(prefix+p.getFileName().toString(), new RegularLogFile(getName(), processName, p));
        }catch(NoSuchFileException e){
            LOGGER.warn("Directory doesn't exist: {}", dir);
        }
    }

    @Override
    public Map<String, LogFile> getServiceLogFiles() throws IOException
    {
        Map<String, LogFile> files = new TreeMap<>();
        
        files.put("journalctl", new Journalctl(getName()));

        readLogFiles(Optional.empty(), logDirectory, files);
        
        return files;
    }
    
    @Override
    public final Map<String, LogFile> getTaskLogFiles(String taskName) throws IOException
    {
        //LOGGER.info("Retriving log files");
        
        Map<String, LogFile> files = new TreeMap<>();
        
        Path dir = pm.getProcessLogDirectory(getName(), taskName);
        if(dir != null)
            readLogFiles(Optional.of(taskName), dir, files);
        else
            LOGGER.info("No log directory for service{} task {}", getName(), taskName);
        
        return files;
    }
    
    static final String TASK_BUILD = "build";
    static final String TASK_DEPLOY = "deploy";

    @Override
    public Iterable<String> getTasks()
    {
        return Arrays.asList(TASK_BUILD, TASK_DEPLOY);
    }
    
    void start(Path workingDirectory, String name, String[] cmd, boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Process p;
        synchronized(this)
        {
            if(isTaskRunning())
                throw new BuildOrDeployAlreadyRunningException("There is already a build/deploy running for the service "+getName());
            
            long startTime = System.currentTimeMillis();
            
            ProcessInfo pi = pm.register(getName(), name, Arrays.asList(cmd), workingDirectory, startTime, initiator);
            Path logFile = pm.getLogFile(pi);
            
            LOGGER.info("Starting: {}", String.join(" ", cmd));

            p = new ProcessBuilder(cmd)
                    .directory(workingDirectory.toFile())
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()))
                    .redirectError(ProcessBuilder.Redirect.to(logFile.toFile()))
                    .start();
            pi.setProcess(p);
            runningTaskProcess = p;
        }
        
        if(!asynch)
        {
            int code = p.waitFor();
            LOGGER.info("Finished with {}", code);
            pm.notifyProcessFinished();
            if(code != 0)
                throw new IOException("Deploy process for "+getName()+" returned "+code);
        }
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
