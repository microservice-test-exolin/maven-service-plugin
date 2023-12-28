package org.exolin.msp.web.ui;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.log.RegularLogFile;
import org.exolin.msp.service.pm.ProcessDataStorage;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.service.stub.StubService;
import org.exolin.msp.service.stub.StubServices;
import static org.exolin.msp.web.ui.Main.run;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;

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
        
        List<String> lines = Arrays.asList(
                "Log Entry",
                "[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ web-ui-api-linux ---",
                "[INFO] Log Info",
                "[WARNING] Log Warning",
                "[ERROR] Log Error",
                "[INFO] ------------------------------------------------------------------------",
                "[INFO] BUILD SUCCESS",
                "[INFO] ------------------------------------------------------------------------"
        );
        
        Files.write(logDirectory.resolve("service.log"), lines);
        Files.write(logDirectory.resolve("task-build-"+TS+".log"), lines);
        Files.write(logDirectory.resolve("task-deploy-"+TS+".log"), lines);
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(logDirectory))
        {
            dir.forEach(d -> {
                System.out.println("Found log file "+d);
                logFiles.put(d.getFileName().toString(), new RegularLogFile("test-mittens-discord", getTaskName(d.getFileName().toString()), d));
            });
        }
        
        ProcessManager pm = new ProcessManager(new ProcessDataStorage(logDirectory));
        
        Path gitRoot = Paths.get("repos").toAbsolutePath();
        String prefix = "http://github.com/a/";
        
        //PseudoAbstraction sys = new PseudoAbstraction(new LogAdapter(PseudoAbstraction.class));
        
        StubService s1 = new StubService("test-mittens-discord", gitRoot.resolve("test-mittens-discord"), gitRoot.resolve("test-mittens-discord"), prefix+"test-mittens-discord", logFiles);
        StubService s2 = new StubService("test-milkboi-discord", gitRoot.resolve("test-milkboi"), gitRoot.resolve("test-milkboi/discord"), prefix+"test-milkboi-discord", Collections.emptyMap());
        StubService s3 = new StubService("test-milkboi-telegram", gitRoot.resolve("test-milkboi"), gitRoot.resolve("test-milkboi/telegram"), prefix+"test-milkboi-telegram", Collections.emptyMap());
        
        Services services = new StubServices(Arrays.asList(s1, s2, s3));
        
        s1.getApplicationInstance().start();
        s2.getApplicationInstance().setFailed();
        
        Process process = new ProcessBuilder("cmd", "/c", "echo x").start();
        
        String initiator1 = GithubWebhookServlet.INITIATER_PREFIX+"["+GithubWebhookServlet.NAME_REPO+"=https://github.com/tomgk/mittens]";
        String initiator2 = DeployServlet.INITIATOR_WEB_UI+"["+DeployServlet.NAME_USER+"=testuser]";
        
        pm.register(services.getServices().get(0).getName(), "build", Arrays.asList("mvn", "build"), Paths.get("."), System.currentTimeMillis(), initiator1).setProcess(process);
        pm.register(services.getServices().get(1).getName(), "build", Arrays.asList("mvn", "build"), Paths.get("."), System.currentTimeMillis(), initiator2).setProcess(process);
        
        run(pm, services, Config.read(Paths.get("../config/config")), Paths.get("../config"), false);
        
        Scanner s = new Scanner(System.in);
        while(s.hasNext())
        {
            if(s.next().equals("x"))
            {
                System.out.println("Exiting...");
                System.exit(0);
            }
            else
            {
                if(System.in.available() > 0)
                    continue;
                
                System.out.println("Unknown input\nEnter x to stop program");
            }
        }
    }
}
