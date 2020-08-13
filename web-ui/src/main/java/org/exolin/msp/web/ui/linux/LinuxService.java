package org.exolin.msp.web.ui.linux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.AbstractService;
import org.exolin.msp.web.ui.LognameGenerator;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxService extends AbstractService
{
    private final Path serviceDirectory;
    
    private final ProcessManager pm;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxService.class);

    public LinuxService(
            Path serviceDirectory,
            Path logDirectory,
            String name,
            SystemAbstraction sys,
            ProcessManager pm)
    {
        super(name, sys, logDirectory);
        this.serviceDirectory = serviceDirectory;
        this.pm = pm;
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
    
    public Path getGitRoot() throws IOException
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
    
    public static final String BUILD_LOG_GROUP = LognameGenerator.creatTaskGroup("build");
    public static final String DEPLOY_LOG_GROUP = LognameGenerator.creatTaskGroup("deploy");
    
    @Override
    public void build(boolean asynch) throws IOException, InterruptedException
    {
        Path dir = getGitRoot();
        
        String[] cmd = {"/bin/bash", "-c", "git pull && mvn package"};
        
        Path logFile = getLogDirectory().resolve(LognameGenerator.generateFilename(BUILD_LOG_GROUP, LocalDateTime.now()));
        
        long startTime = System.currentTimeMillis();
        Process p = new ProcessBuilder(cmd)
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()))
                .redirectError(ProcessBuilder.Redirect.to(logFile.toFile()))
                .start();
        
        pm.register(getName(), p, Arrays.asList(cmd), "Building "+getName(), startTime);
        
        if(!asynch)
        {
            int code = p.waitFor();
            if(code != 0)
                throw new IOException("Build process for "+getName()+" returned "+code);
        }
    }
    
    @Override
    public void deploy(boolean asynch) throws IOException, InterruptedException
    {
        Path dir = getOriginalPath();
        
        String[] cmd = {"/bin/bash", "-c", "/root/repos/deploy.sh"};
        
        Path logFile = getLogDirectory().resolve(LognameGenerator.generateFilename(BUILD_LOG_GROUP, LocalDateTime.now()));
        
        long startTime = System.currentTimeMillis();
        Process p = new ProcessBuilder(cmd)
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()))
                .redirectError(ProcessBuilder.Redirect.to(logFile.toFile()))
                .start();
        
        pm.register(getName(), p, Arrays.asList(cmd), "Deploying "+getName(), startTime);
        
        if(!asynch)
        {
            int code = p.waitFor();
            if(code != 0)
                throw new IOException("Deploy process for "+getName()+" returned "+code);
        }
    }
    
    @Override
    public String getRepositoryUrl() throws IOException
    {
        return getRepositoryUrl(getGitRoot());
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
