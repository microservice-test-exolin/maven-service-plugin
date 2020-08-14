package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.google.MyOAuthCallbackServlet;
import org.exolin.msp.web.ui.google.MyOAuthFilter;
import org.exolin.msp.web.ui.linux.LinuxServices;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.ProcessServlet;
import org.exolin.msp.web.ui.servlet.ResourceServlet;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.servlet.github.GithubDeployerImpl;
import org.exolin.msp.web.ui.servlet.github.GithubServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemEnvironmentServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemPropertiesServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceStatusServlet;

/**
 *
 * @author tomgk
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        ProcessManager pm = new ProcessManager();
        
        SystemAbstraction sys = new LinuxAbstraction(new Log()
        {
            @Override
            public void warn(String string){}
            @Override
            public void info(String string){}
        });//new LogAdapter(LinuxAbstraction.class));
            
        LinuxServices services = new LinuxServices(Paths.get("/home/exolin/services"), sys, pm);
        
        run(pm, sys, services);
    }
    
    public static void run(ProcessManager pm, SystemAbstraction sys, Services services) throws Exception
    {
        GithubDeployerImpl githubDeployer = new GithubDeployerImpl(new String(Files.readAllBytes(Paths.get("../config/github.token").toAbsolutePath().normalize())).trim());
        
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("127.0.0.1");
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});
        
        Runtime.getRuntime().addShutdownHook(new Thread("childprocess.kill"){
            @Override
            public void run()
            {
                pm.killAll();
            }
        });
        
        server.setSessionIdManager(new DefaultSessionIdManager(server));
        
        ServletHandler servletHandler = new ServletHandler();
        
        // Add OAuth2 callback servlet.
        servletHandler.addServletWithMapping(MyOAuthCallbackServlet.class, "/callback");

        // Add filters for app servlet
        servletHandler.addFilterWithMapping(MyOAuthFilter.class, "/app/*",
                                            EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
        
        servletHandler.addServletWithMapping(IndexServlet.class, "/");
        
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        
        servletHandler.addServletWithMapping(ResourceServlet.class, "/favicon.png").setServlet(new ResourceServlet("image/png", "files/favicon.png"));
        servletHandler.addServletWithMapping(ResourceServlet.class, "/favicon.ico").setServlet(new ResourceServlet("image/x-icon", "files/favicon.ico"));
        servletHandler.addServletWithMapping(ResourceServlet.class, "/dashboard.css").setServlet(new ResourceServlet("text/css", "files/dashboard.css"));
        
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/services").setServlet(new ListServicesServlet(services));
        servletHandler.addServletWithMapping(DeployServlet.class, "/deploy").setServlet(new DeployServlet(services));
        servletHandler.addServletWithMapping(LogServlet.class, "/logs").setServlet(new LogServlet(services));
        servletHandler.addServletWithMapping(ProcessServlet.class, "/processes").setServlet(new ProcessServlet(pm));
        servletHandler.addServletWithMapping(ServiceStatusServlet.class, ServiceStatusServlet.URL).setServlet(new ServiceStatusServlet(services));
        
        servletHandler.addServletWithMapping(GithubServlet.class, "/github").setServlet(new GithubServlet(services, githubDeployer));
        
        servletHandler.addServletWithMapping(ServerInfoServlet.class, ServerInfoServlet.URL);
        servletHandler.addServletWithMapping(SystemPropertiesServlet.class, SystemPropertiesServlet.URL);
        servletHandler.addServletWithMapping(SystemEnvironmentServlet.class, SystemEnvironmentServlet.URL);
        
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setHandler(servletHandler);
        sessionHandler.setSessionIdManager(server.getSessionIdManager());
        server.setHandler(sessionHandler);
        
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
}
