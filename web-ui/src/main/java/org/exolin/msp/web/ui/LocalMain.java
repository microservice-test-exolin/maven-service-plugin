package org.exolin.msp.web.ui;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.NoProcessStore;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.service.stub.StubService;
import org.exolin.msp.service.stub.StubServices;
import static org.exolin.msp.web.ui.Main.run;

/**
 *
 * @author tomgk
 */
public class LocalMain
{
    public static void main(String[] args) throws Exception
    {
        String TS = "2020-01-02-030405";
        
        Path logDirectory = Paths.get("log");
        if(!Files.exists(logDirectory))
            Files.createDirectory(logDirectory);
        
        Map<String, Path> logFiles = new HashMap<>();
        
        Files.write(logDirectory.resolve("service.log"), Arrays.asList("Log Entry"));
        Files.write(logDirectory.resolve("task-build-"+TS+".log"), Arrays.asList("Log Entry"));
        Files.write(logDirectory.resolve("task-deploy-"+TS+".log"), Arrays.asList("Log Entry"));
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(logDirectory))
        {
            dir.forEach(d -> logFiles.put(d.getFileName().toString(), d));
        }
        
        ProcessManager pm = new ProcessManager(new NoProcessStore());
        
        SystemAbstraction sys = new PseudoAbstraction(new LogAdapter(PseudoAbstraction.class));
        Services services = new StubServices(Arrays.asList(
                    new StubService("test-mittens-discord", sys, logFiles),
                    new StubService("test-milkboi-discord", sys, logFiles),
                    new StubService("test-milkboi-telegram", sys, logFiles)
            ));
        
        run(pm, sys, services, Config.read(Paths.get("../config/config")));
    }
}
