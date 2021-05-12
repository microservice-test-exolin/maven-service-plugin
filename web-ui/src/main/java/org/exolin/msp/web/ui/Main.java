package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.linux.LinuxServices;
import org.exolin.msp.service.pm.ProcessDataStorage;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.ResourceServlet;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.servlet.UnsupportedServlet;
import org.exolin.msp.web.ui.servlet.auth.AuthFilter;
import org.exolin.msp.web.ui.servlet.auth.GithubLogoutServlet;
import org.exolin.msp.web.ui.servlet.auth.GithubOAuth;
import org.exolin.msp.web.ui.servlet.auth.GithubOAuthServlet;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.exolin.msp.web.ui.servlet.github.api.GithubDeployerImpl;
import org.exolin.msp.web.ui.servlet.log.ServiceLogFileShowServlet;
import org.exolin.msp.web.ui.servlet.log.TaskLogFileShowServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemEnvironmentServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemPropertiesServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceConfigServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceLogServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceStatusServlet;
import org.exolin.msp.web.ui.servlet.task.ProcessHistoryServlet;
import org.exolin.msp.web.ui.servlet.task.ProcessServlet;
import org.exolin.msp.web.ui.servlet.task.TaskLogServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static LocalDateTime startedAt = LocalDateTime.now();
    
    public static void main(String[] args) throws Exception
    {
        String configDir = System.getenv("SERVICE_CFG_DIR");
        if(configDir == null)
        {
            System.err.println("Environment variable SERVICE_CFG_DIR not set");
            System.exit(-1);
        }
        
        try{
            ProcessManager pm = new ProcessManager(new ProcessDataStorage(Paths.get("../data")));

            //SystemAbstraction sys = new LinuxAbstraction(new LogAdapter(LinuxAbstraction.class));

            LinuxServices services = new LinuxServices(
                    Paths.get("/home/exolin/services"),
                    Paths.get("/root/apps"),
                    Paths.get("/root/repos"),
                    pm);

            run(pm, services, Config.read(Paths.get(configDir, "config")), Paths.get(configDir), true);
        }catch(Exception e){
            LOGGER.error("Error starting", e);
            throw e;
        }
    }
    
    public static void run(ProcessManager pm, Services services, Config config, Path configDir, boolean localhost) throws Exception
    {
        ScheduledExecutorService deamonScheduler = Executors.newScheduledThreadPool(1, (Runnable r) -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    
        deamonScheduler.scheduleWithFixedDelay(pm::clean, 1, 1, TimeUnit.SECONDS);
        
        int port = 8090;
        Server server = create(pm, services, config, configDir, port, localhost);
        
        try{
            server.start();
        }catch(IOException e){
            try{
                server.stop();
            }catch(Exception e2){
                e.addSuppressed(e2);
            }
            throw e;
        }
        
        System.out.println("Started on port "+port);
    }
    
    public static Server create(ProcessManager pm, Services services, Config config, Path configDir, int port, boolean localhost) throws Exception
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Path githubTokenFile = configDir.resolve("github.token");
        
        GithubDeployerImpl githubDeployer = null;
        try{
            githubDeployer = new GithubDeployerImpl(new String(Files.readAllBytes(githubTokenFile)).trim());
        }catch(NoSuchFileException e){
            LOGGER.warn("No github webhook because there's no {}", githubTokenFile.toAbsolutePath().normalize());
        }
        
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        if(localhost)
            connector.setHost("127.0.0.1");
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        
        Runtime.getRuntime().addShutdownHook(new Thread("childprocess.kill"){
            @Override
            public void run()
            {
                pm.killAll();
            }
        });
        
        ServletHandler servletHandler = new ServletHandler();
        
        switch(config.get(Config.KEY_AUTH_TYPE, Config.AuthType.class))
        {
            case github:
                GithubOAuth githubOAuth = new GithubOAuth(config.get(Config.KEY_GITHUB_CLIENT_ID), config.get(Config.KEY_GITHUB_CLIENT_SECRET));
                Set<String> allowedUsers = config.getStringSet(Config.ALLOWED_USERS);
                servletHandler.addFilterWithMapping(AuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)).setFilter(new AuthFilter(githubOAuth, allowedUsers));
                servletHandler.addServletWithMapping(GithubOAuthServlet.class, GithubOAuthServlet.URL).setServlet(new GithubOAuthServlet(githubOAuth));
                servletHandler.addServletWithMapping(GithubLogoutServlet.class, GithubLogoutServlet.URL);
                break;
                
            case none:
                break;
                
            default:
                throw new UnsupportedOperationException();
        }
        
        servletHandler.addServletWithMapping(IndexServlet.class, "/*").setServlet(new IndexServlet(services, pm));
        
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        
        ResourceServlet.addFile(servletHandler, "favicon.png", "image/png");
        ResourceServlet.addFile(servletHandler, "favicon.ico", "image/x-icon");
        ResourceServlet.addFile(servletHandler, "dashboard.css", "text/css");
        ResourceServlet.addFile(servletHandler, "log.css", "text/css");
        ResourceServlet.addFile(servletHandler, "GitHub_Logo_small.png", "image/png");
        
        servletHandler.addServletWithMapping(ServiceServlet.class, ServiceServlet.URL).setServlet(new ServiceServlet(services, pm));
        servletHandler.addServletWithMapping(ServiceConfigServlet.class, ServiceConfigServlet.URL).setServlet(new ServiceConfigServlet(services));
        servletHandler.addServletWithMapping(ListServicesServlet.class, ListServicesServlet.URL).setServlet(new ListServicesServlet(services));
        servletHandler.addServletWithMapping(DeployServlet.class, DeployServlet.URL).setServlet(new DeployServlet(services));
        servletHandler.addServletWithMapping(ServiceLogServlet.class, ServiceLogServlet.URL).setServlet(new ServiceLogServlet(services));
        servletHandler.addServletWithMapping(TaskLogServlet.class, TaskLogServlet.URL).setServlet(new TaskLogServlet(services));
        servletHandler.addServletWithMapping(ServiceLogFileShowServlet.class, ServiceLogFileShowServlet.URL).setServlet(new ServiceLogFileShowServlet(services));
        servletHandler.addServletWithMapping(TaskLogFileShowServlet.class, TaskLogFileShowServlet.URL).setServlet(new TaskLogFileShowServlet(services));
        servletHandler.addServletWithMapping(ProcessServlet.class, ProcessServlet.URL).setServlet(new ProcessServlet(pm));
        servletHandler.addServletWithMapping(ProcessHistoryServlet.class, ProcessHistoryServlet.URL).setServlet(new ProcessHistoryServlet(pm));
        servletHandler.addServletWithMapping(ServiceStatusServlet.class, ServiceStatusServlet.URL).setServlet(new ServiceStatusServlet(services));
        
        if(githubDeployer != null)
            servletHandler.addServletWithMapping(GithubWebhookServlet.class, GithubWebhookServlet.URL).setServlet(new GithubWebhookServlet(services, githubDeployer, executorService));
        else
            servletHandler.addServletWithMapping(UnsupportedServlet.class, GithubWebhookServlet.URL).setServlet(new UnsupportedServlet("Github hooks aren't supported"));
        
        servletHandler.addServletWithMapping(ServerInfoServlet.class, ServerInfoServlet.URL);
        servletHandler.addServletWithMapping(SystemPropertiesServlet.class, SystemPropertiesServlet.URL);
        servletHandler.addServletWithMapping(SystemEnvironmentServlet.class, SystemEnvironmentServlet.URL);
        
        server.setHandler(servletHandler);
        
        //enable sessions
        SessionIdManager idmanager = new DefaultSessionIdManager(server);
        server.setSessionIdManager(idmanager);
        SessionHandler sessionsHandler = new SessionHandler();       
        servletHandler.setHandler(sessionsHandler);
        
        Logger requestLogger = LoggerFactory.getLogger("jetty.requestLog");
        
        server.setRequestLog(new RequestLog() {
            @Override
            public void log(Request request, Response response)
            {
                requestLogger.info("{} {} => {} {} {}",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), response.getReason(), response.getContentType());
            }
        });
        
        /*CustomRequestLog requestLog = new CustomRequestLog("/var/logs/jetty/jetty-yyyy_mm_dd.request.log", CustomRequestLog.NCSA_FORMAT);
        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays("90");

        server.setRequestLog(requestLog);*/
        
        return server;
    }
}
