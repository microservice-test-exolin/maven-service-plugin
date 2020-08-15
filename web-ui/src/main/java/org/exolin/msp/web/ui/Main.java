package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.SystemAbstraction;
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
        Server server = create(pm, sys, services, 8090);
        
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
    
    public static Server create(ProcessManager pm, SystemAbstraction sys, Services services, int port) throws Exception
    {
        GithubDeployerImpl githubDeployer = new GithubDeployerImpl(new String(Files.readAllBytes(Paths.get("../config/github.token"))).trim());
        
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
        
        servletHandler.addServletWithMapping(IndexServlet.class, "/*");
        
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        
        ResourceServlet.addFile(servletHandler, "favicon.png", "image/png");
        ResourceServlet.addFile(servletHandler, "favicon.ico", "image/x-icon");
        ResourceServlet.addFile(servletHandler, "dashboard.css", "text/css");
        
        servletHandler.addServletWithMapping(ListServicesServlet.class, ListServicesServlet.URL).setServlet(new ListServicesServlet(services));
        servletHandler.addServletWithMapping(DeployServlet.class, DeployServlet.URL).setServlet(new DeployServlet(services));
        servletHandler.addServletWithMapping(LogServlet.class, LogServlet.URL).setServlet(new LogServlet(services));
        servletHandler.addServletWithMapping(ProcessServlet.class, ProcessServlet.URL).setServlet(new ProcessServlet(pm));
        servletHandler.addServletWithMapping(ServiceStatusServlet.class, ServiceStatusServlet.URL).setServlet(new ServiceStatusServlet(services));
        
        servletHandler.addServletWithMapping(GithubServlet.class, GithubServlet.URL).setServlet(new GithubServlet(services, githubDeployer));
        
        servletHandler.addServletWithMapping(ServerInfoServlet.class, ServerInfoServlet.URL);
        servletHandler.addServletWithMapping(SystemPropertiesServlet.class, SystemPropertiesServlet.URL);
        servletHandler.addServletWithMapping(SystemEnvironmentServlet.class, SystemEnvironmentServlet.URL);
        
        server.setHandler(servletHandler);
        
        return server;
    }
}
