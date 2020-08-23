package org.exolin.msp.web.ui;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessDataStorage;
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
    private static Optional<String> getTaskName(String str)
    {
        if(str.startsWith("task-build-"))
            return Optional.of("build");
        if(str.startsWith("task-deploy-"))
            return Optional.of("deploy");
        else
            return Optional.empty();
    }
    
    public static void main(String[] args) throws Exception
    {
        ConsoleLog.setup();
        
        String TS = "2020-01-02-030405";
        
        Path logDirectory = Paths.get("log");
        if(!Files.exists(logDirectory))
            Files.createDirectory(logDirectory);
        
        Map<String, LogFile> logFiles = new HashMap<>();
        
        Files.write(logDirectory.resolve("service.log"), Arrays.asList("Log Entry"));
        Files.write(logDirectory.resolve("task-build-"+TS+".log"), Arrays.asList("Log Entry"));
        Files.write(logDirectory.resolve("task-deploy-"+TS+".log"), Arrays.asList("Log Entry"));
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(logDirectory))
        {
            dir.forEach(d -> logFiles.put(d.getFileName().toString(), new LogFile("test-mittens-discord", getTaskName(d.getFileName().toString()), d)));
        }
        
        ProcessManager pm = new ProcessManager(new ProcessDataStorage(logDirectory));
        
        Path gitRoot = Paths.get("repos").toAbsolutePath();
        String prefix = "http://github.com/a/";
        
        PseudoAbstraction sys = new PseudoAbstraction(new LogAdapter(PseudoAbstraction.class));
        
        Services services = new StubServices(Arrays.asList(
                    new StubService("test-mittens-discord", gitRoot.resolve("test-mittens-discord"), gitRoot.resolve("test-mittens-discord"), prefix+"test-mittens-discord", sys, logFiles),
                    new StubService("test-milkboi-discord", gitRoot.resolve("test-milkboi"), gitRoot.resolve("test-milkboi/discord"), prefix+"test-milkboi-discord", sys, Collections.emptyMap()),
                    new StubService("test-milkboi-telegram", gitRoot.resolve("test-milkboi"), gitRoot.resolve("test-milkboi/telegram"), prefix+"test-milkboi-telegram", sys, Collections.emptyMap())
            ));
        
        sys.start("test-mittens-discord");
        sys.setFailed("test-milkboi-discord");
        
        Process process = new ProcessBuilder("cmd", "/c", "echo x").start();
        
        pm.register(services.getServices().get(0).getName(), "build", Arrays.asList("mvn", "build"), Paths.get("."), System.currentTimeMillis(), "testcode").setProcess(process);
        pm.register(services.getServices().get(1).getName(), "build", Arrays.asList("mvn", "build"), Paths.get("."), System.currentTimeMillis(), "testcode").setProcess(process);
        
        run(pm, sys, services, Config.read(Paths.get("../config/config")), false);
    }
}
