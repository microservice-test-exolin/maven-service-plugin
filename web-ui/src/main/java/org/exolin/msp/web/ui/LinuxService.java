package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.stub.StubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LinuxService extends StubService
{
    private final Path serviceDirectory;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxService.class);

    public LinuxService(Path serviceDirectory, String name, SystemAbstraction sys)
    {
        super(name, sys);
        this.serviceDirectory = serviceDirectory;
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
    
    public static final String BUILD_LOG = "build.out.log";
    public static final String DEPLOY_LOG = "deploy.out.log";
    
    private Path getBuildOut()
    {
        return serviceDirectory.resolve("log").resolve(BUILD_LOG);
    }
    
    private Path getBuildErr()
    {
        return serviceDirectory.resolve("log").resolve("build.err.log");
    }
    
    private Path getDeployOut()
    {
        return serviceDirectory.resolve("log").resolve(DEPLOY_LOG);
    }
    
    private Path getDeployErr()
    {
        return serviceDirectory.resolve("log").resolve("deploy.err.log");
    }

    @Override
    public Map<String, Path> getLogFiles() throws IOException
    {
        Map<String, Path> files = new TreeMap<>();
        
        Path logDir = serviceDirectory.resolve("log");
        
        LOGGER.info("Reading log file list for {} from {}", getName(), logDir);
        
        for(Path p: Files.newDirectoryStream(logDir))
        {
            LOGGER.info("- {}", p.getFileName());
            files.put(p.getFileName().toString(), p);
        }
        
        return files;
    }
    
    @Override
    public void build(ProcessManager pm) throws IOException, InterruptedException
    {
        Path dir = getOriginalPath();
        
        String[] cmd = {"/bin/bash", "-c", "git pull && mvn package"};
        
        long startTime = System.currentTimeMillis();
        Process p = new ProcessBuilder(cmd)
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(getBuildOut().toFile()))
                .redirectError(ProcessBuilder.Redirect.to(getBuildErr().toFile()))
                .start();
        
        pm.register(getName(), p, Arrays.asList(cmd), "Building "+getName(), startTime);
    }
    
    @Override
    public void deploy(ProcessManager pm) throws IOException, InterruptedException
    {
        Path dir = getOriginalPath();
        
        String[] cmd = {"/bin/bash", "-c", "/root/repos/deploy.sh"};
        
        long startTime = System.currentTimeMillis();
        Process p = new ProcessBuilder(cmd)
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(getDeployOut().toFile()))
                .redirectError(ProcessBuilder.Redirect.to(getDeployErr().toFile()))
                .start();
        
        pm.register(getName(), p, Arrays.asList(cmd), "Deploying "+getName(), startTime);
    }
}
