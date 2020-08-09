package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
    
    @Override
    public void build(List<String> log) throws IOException, InterruptedException
    {
        build(getOriginalPath(), log);
    }
    
    public static void build(Path dir, List<String> log) throws IOException, InterruptedException
    {
        Process p = new ProcessBuilder("/bin/bash", "-c", "git pull && mvn package")
                .directory(dir.toFile())
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
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
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        
        String out = LinuxAbstraction.read(p);
        log.addAll(Arrays.asList(out.split("\n")));
    }
}
