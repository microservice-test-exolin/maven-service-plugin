package org.exolin.msp.service.linux;

import org.exolin.msp.service.log.RegularLogFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.pm.TaskAlreadyRunningException;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public abstract class AbstractLinuxService implements Service
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLinuxService.class);
    
    private final String name;
    private Process runningTaskProcess;
    
    final ProcessManager pm;
    
    public AbstractLinuxService(String name, ProcessManager pm)
    {
        this.name = name;
        this.pm = pm;
    }

    @Override
    public String getName()
    {
        return name;
    }

    boolean isTaskRunning()
    {
        return runningTaskProcess != null && runningTaskProcess.isAlive();
    }
    
    void start(Path workingDirectory, String name, String[] cmd, boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Process p;
        synchronized(this)
        {
            if(isTaskRunning())
                throw new TaskAlreadyRunningException("There is already a build/deploy running for the service "+getName());
            
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

    void readLogFiles(Optional<String> processName, Path dir, Map<String, LogFile> files) throws IOException
    {
        String prefix = processName.map(s -> "task/"+s+"/").orElse("service/");
        
        try{
            for(Path p: Files.newDirectoryStream(dir, "*.log"))
                files.put(prefix+p.getFileName().toString(), new RegularLogFile(getName(), processName, p));
        }catch(NoSuchFileException e){
            LOGGER.warn("Directory doesn't exist: {}", dir);
        }
    }
}

