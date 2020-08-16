package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.linux.LinuxServices;
import org.exolin.msp.service.pm.ProcessDataStorage;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.ProcessServlet;
import org.exolin.msp.web.ui.servlet.ResourceServlet;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.servlet.UnsupportedServlet;
import org.exolin.msp.web.ui.servlet.auth.AuthFilter;
import org.exolin.msp.web.ui.servlet.auth.GithubOAuth;
import org.exolin.msp.web.ui.servlet.auth.GithubOAuthServlet;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.exolin.msp.web.ui.servlet.github.api.GithubDeployerImpl;
import org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemEnvironmentServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemPropertiesServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogFileShowServlet;
import org.exolin.msp.web.ui.servlet.service.LogServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceStatusServlet;
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
        try{
            ProcessManager pm = new ProcessManager(new ProcessDataStorage(Paths.get("../data")));

            SystemAbstraction sys = new LinuxAbstraction(new Log()
            {
                @Override
                public void warn(String string){}
                @Override
                public void info(String string){}
            });//new LogAdapter(LinuxAbstraction.class));

            LinuxServices services = new LinuxServices(
                    Paths.get("/home/exolin/services"),
                    sys, pm);

            run(pm, sys, services, Config.read(Paths.get("../config/config")));
        }catch(Exception e){
            LOGGER.error("Error starting", e);
            throw e;
        }
    }
    
    public static void run(ProcessManager pm, SystemAbstraction sys, Services services, Config config) throws Exception
    {
        ScheduledExecutorService deamonScheduler = Executors.newScheduledThreadPool(1, (Runnable r) -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    
        deamonScheduler.scheduleWithFixedDelay(pm::clean, 1, 1, TimeUnit.SECONDS);
        
        Server server = create(pm, sys, services, config, 8090);
        
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
    }
    
    public static Server create(ProcessManager pm, SystemAbstraction sys, Services services, Config config, int port) throws Exception
    {
        Path githubTokenFile = Paths.get("../config/github.token");
        
        GithubDeployerImpl githubDeployer = null;
        try{
            githubDeployer = new GithubDeployerImpl(new String(Files.readAllBytes(githubTokenFile)).trim());
        }catch(NoSuchFileException e){
            LOGGER.warn("No github webhook because there's no {}", githubTokenFile.toAbsolutePath().normalize());
        }
        
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
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
                servletHandler.addFilterWithMapping(AuthFilter.class, "/", EnumSet.of(DispatcherType.REQUEST)).setFilter(new AuthFilter(githubOAuth, allowedUsers));
                servletHandler.addServletWithMapping(GithubOAuthServlet.class, GithubOAuthServlet.URL).setServlet(new GithubOAuthServlet(githubOAuth));
                break;
                
            case none:
                break;
                
            default:
                throw new UnsupportedOperationException();
        }
        
        servletHandler.addServletWithMapping(IndexServlet.class, "/*");
        
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        
        ResourceServlet.addFile(servletHandler, "favicon.png", "image/png");
        ResourceServlet.addFile(servletHandler, "favicon.ico", "image/x-icon");
        ResourceServlet.addFile(servletHandler, "dashboard.css", "text/css");
        
        servletHandler.addServletWithMapping(ServiceServlet.class, ServiceServlet.URL).setServlet(new ServiceServlet(services, pm));
        servletHandler.addServletWithMapping(ListServicesServlet.class, ListServicesServlet.URL).setServlet(new ListServicesServlet(services));
        servletHandler.addServletWithMapping(DeployServlet.class, DeployServlet.URL).setServlet(new DeployServlet(services));
        servletHandler.addServletWithMapping(LogServlet.class, LogServlet.URL).setServlet(new LogServlet(services));
        servletHandler.addServletWithMapping(LogFileShowServlet.class, LogFileShowServlet.URL).setServlet(new LogFileShowServlet(services));
        servletHandler.addServletWithMapping(ProcessServlet.class, ProcessServlet.URL).setServlet(new ProcessServlet(pm));
        servletHandler.addServletWithMapping(ServiceStatusServlet.class, ServiceStatusServlet.URL).setServlet(new ServiceStatusServlet(services));
        
        if(githubDeployer != null)
            servletHandler.addServletWithMapping(GithubWebhookServlet.class, GithubWebhookServlet.URL).setServlet(new GithubWebhookServlet(services, githubDeployer));
        else
            servletHandler.addServletWithMapping(UnsupportedServlet.class, GithubWebhookServlet.URL);
        
        servletHandler.addServletWithMapping(ServerInfoServlet.class, ServerInfoServlet.URL);
        servletHandler.addServletWithMapping(SystemPropertiesServlet.class, SystemPropertiesServlet.URL);
        servletHandler.addServletWithMapping(SystemEnvironmentServlet.class, SystemEnvironmentServlet.URL);
        
        server.setHandler(servletHandler);
        
        //enable sessions
        SessionIdManager idmanager = new DefaultSessionIdManager(server);
        server.setSessionIdManager(idmanager);
        SessionHandler sessionsHandler = new SessionHandler();       
        servletHandler.setHandler(sessionsHandler);           
        
        return server;
    }
}
