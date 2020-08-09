package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.stub.StubService;

/**
 *
 * @author tomgk
 */
public class LinuxService extends StubService
{
    private final Path serviceDirectory;

    public LinuxService(Path serviceDirectory, String name, SystemAbstraction sys)
    {
        super(name, sys);
        this.serviceDirectory = serviceDirectory;
    }
    
    private Path getOriginalPath() throws IOException
    {
        Path originalPathFile = serviceDirectory.resolve("original.path");
        return Paths.get(new String(Files.readAllBytes(originalPathFile), StandardCharsets.UTF_8));
    }
    
    private Path getBuildOut()
    {
        return serviceDirectory.resolve("log").resolve("build.out.log");
    }
    
    private Path getBuildErr()
    {
        return serviceDirectory.resolve("log").resolve("build.err.log");
    }
    
    private Path getDeployOut()
    {
        return serviceDirectory.resolve("log").resolve("deploy.out.log");
    }
    
    private Path getDeployErr()
    {
        return serviceDirectory.resolve("log").resolve("deploy.err.log");
    }

    @Override
    public Map<String, Path> getLogFiles() throws IOException
    {
        Map<String, Path> files = new HashMap<>();
        
        for(Path p: Files.newDirectoryStream(serviceDirectory.resolve("log")))
            files.put(p.getFileName().toString(), p);
        
        return files;
    }
    
    @Override
    public void build(List<String> log) throws IOException, InterruptedException
    {
        Path dir = getOriginalPath();
        
        Process p = new ProcessBuilder("/bin/bash", "-c", "git pull && mvn package")
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(getBuildOut().toFile()))
                .redirectError(ProcessBuilder.Redirect.to(getBuildErr().toFile()))
                .start();
        
        String out = LinuxAbstraction.read(p);
        log.addAll(Arrays.asList(out.split("\n")));
    }
    
    @Override
    public void deploy(List<String> log) throws IOException, InterruptedException
    {
        Path dir = getOriginalPath();
        
        Process p = new ProcessBuilder("/bin/bash", "-c", "/root/repos/deploy.sh")
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.to(getDeployOut().toFile()))
                .redirectError(ProcessBuilder.Redirect.to(getDeployErr().toFile()))
                .start();
        
        String out = LinuxAbstraction.read(p);
        log.addAll(Arrays.asList(out.split("\n")));
    }
}
