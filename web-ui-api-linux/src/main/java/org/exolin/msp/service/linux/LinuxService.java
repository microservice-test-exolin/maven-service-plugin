package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.AbstractService;
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
    
    private final ProcessManager pm;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxService.class);
    
    private Process runningBuildOrDeployProcess;
    
    public LinuxService(
            Path serviceDirectory,
            Path logDirectory,
            String name,
            SystemAbstraction sys,
            ProcessManager pm)
    {
        super(name, sys);
        this.serviceDirectory = serviceDirectory;
        this.logDirectory = logDirectory;
        this.pm = pm;
    }

    @Override
    public boolean isBuildOrDeployProcessRunning()
    {
        return runningBuildOrDeployProcess != null && runningBuildOrDeployProcess.isAlive();
    }
    
    @Override
    public boolean supportsBuildAndDeployment() throws IOException
    {
        try{
            getOriginalPath();
            return true;
        }catch(UnsupportedOperationException e){
            LOGGER.info("Couldn't determine original path", e);
            return false;
        }
    }
    
    private Path getOriginalPath() throws IOException
    {
        Path originalPathFile = serviceDirectory.resolve("original.path");
        try{
            return Paths.get(new String(Files.readAllBytes(originalPathFile), StandardCharsets.UTF_8));
        }catch(NoSuchFileException e){
            throw new UnsupportedOperationException("Can't determine original path of "+getName(), e);
        }
    }

    private void readLogFiles(Optional<String> processName, Path dir, Map<String, LogFile> files) throws IOException
    {
        String prefix = processName.map(s -> "task/"+s+"/").orElse("service/");
        
        try{
            for(Path p: Files.newDirectoryStream(dir, "*.log"))
                files.put(prefix+p.getFileName().toString(), new LogFile(getName(), processName, p));
        }catch(NoSuchFileException e){
            LOGGER.warn("Directory doesn't exist: {}", dir);
        }
    }
    
    @Override
    public final Map<String, LogFile> getLogFiles(Optional<String> taskName) throws IOException
    {
        //LOGGER.info("Retriving log files");
        
        Map<String, LogFile> files = new TreeMap<>();
        
        if(taskName == null || !taskName.isPresent())
            readLogFiles(Optional.empty(), logDirectory, files);
        
        for(Map.Entry<String, Path> e: pm.getProcessLogDirectories(getName()).entrySet())
        {
            if(taskName == null || taskName.equals(Optional.of(e.getKey())))
                readLogFiles(Optional.of(e.getKey()), e.getValue(), files);
        }
        
        return files;
    }
    
    @Override
    public Path getLocalGitRoot() throws IOException
    {
        return getGitRoot(getOriginalPath());
    }
    
    static Path getGitRoot(Path path)
    {
        for(Path p=path;p!=null;p=p.getParent())
        {
            if(Files.exists(p.resolve(".git")))
                return p;
        }
        
        throw new IllegalArgumentException("Not a git repository: "+path);
    }
    
    @Override
    public void build(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path gitRoot = getLocalGitRoot();
        
        String[] cmd = {"/bin/bash", "-c", "git pull && mvn package"};
        
        start(gitRoot, "build", cmd, asynch, initiator);
    }
    
    @Override
    public void deploy(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path serviceSrcDirectory = getOriginalPath();
        
        String[] cmd = {"/bin/bash", "-c", "/root/repos/deploy.sh"};
        
        start(serviceSrcDirectory, "deploy", cmd, asynch, initiator);
    }
    
    private void start(Path workingDirectory, String name, String[] cmd, boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Process p;
        synchronized(this)
        {
            if(isBuildOrDeployProcessRunning())
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
            runningBuildOrDeployProcess = p;
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
    public String getRepositoryUrl() throws IOException
    {
        return getRepositoryUrl(getLocalGitRoot());
    }
    
    private static int findFirstStartingWith(List<String> list, String startString, int start, int end)
    {
        for(int i=start;i<end;++i)
            if(list.get(i).trim().startsWith(startString))
                return i;
        
        return -1;
    }
    
    public static String getRepositoryUrl(Path gitRepository) throws IOException
    {
        /*
        [remote "origin"]
        url = $URL
        */
        
        List<String> lines = Files.readAllLines(gitRepository.resolve(".git/config"));
        
        String URL = "url = ";
        
        int sectionLine = lines.indexOf("[remote \"origin\"]");
        if(sectionLine == -1)
            throw new IOException("No section remote origin");
        
        int nextSectionLine = findFirstStartingWith(lines, "[", sectionLine+1, lines.size());
        if(nextSectionLine == -1) nextSectionLine = lines.size();
        
        int urlLine = findFirstStartingWith(lines, URL, sectionLine+1, lines.size());
        if(urlLine == -1 || urlLine > nextSectionLine)  //nicht in (richtiger) section gefundne
            throw new IOException("no remote origin url\n"+
                    "sectionLine:"+sectionLine+"\n"+
                    "URlLine:"+urlLine+"\n"+
                    "nextSectionLine:"+nextSectionLine+"\n"+
                    String.join("\n", lines));
        
        String repo = lines.get(urlLine).trim().substring(URL.length());
        if(repo.endsWith(".git"))
            repo = repo.substring(0, repo.length()-4);
        
        return repo;
    }
}
