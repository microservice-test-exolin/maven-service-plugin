package org.exolin.msp.web.ui;

import java.io.IOException;
import java.nio.file.Paths;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.linux.LinuxServices;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.ProcessServlet;
import org.exolin.msp.web.ui.servlet.ResourceServlet;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemEnvironmentServlet;
import org.exolin.msp.web.ui.servlet.serverinfo.SystemPropertiesServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogServlet;

/**
 *
 * @author tomgk
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        SystemAbstraction sys = new LinuxAbstraction(new LogAdapter(LinuxAbstraction.class));
            
        LinuxServices services = new LinuxServices(Paths.get("/home/exolin/services"), sys);
        
        run(sys, services);
    }
    
    public static void run(SystemAbstraction sys, Services services) throws Exception
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});
        
        ProcessManager pm = new ProcessManager();
        
        Runtime.getRuntime().addShutdownHook(new Thread("childprocess.kill"){
            @Override
            public void run()
            {
                pm.killAll();
            }
        });
        
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(IndexServlet.class, "/");
        
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        
        servletHandler.addServletWithMapping(ResourceServlet.class, "/favicon.png").setServlet(new ResourceServlet("image/png", "files/favicon.png"));
        servletHandler.addServletWithMapping(ResourceServlet.class, "/favicon.ico").setServlet(new ResourceServlet("image/x-icon", "files/favicon.ico"));
        servletHandler.addServletWithMapping(ResourceServlet.class, "/dashboard.css").setServlet(new ResourceServlet("text/css", "files/dashboard.css"));
        
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/services").setServlet(new ListServicesServlet(services));
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/deploy").setServlet(new DeployServlet(services, pm));
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/logs").setServlet(new LogServlet(services));
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/processes").setServlet(new ProcessServlet(pm));
        
        servletHandler.addServletWithMapping(ServerInfoServlet.class, ServerInfoServlet.URL);
        servletHandler.addServletWithMapping(SystemPropertiesServlet.class, SystemPropertiesServlet.URL);
        servletHandler.addServletWithMapping(SystemEnvironmentServlet.class, SystemEnvironmentServlet.URL);
        
        server.setHandler(servletHandler);
        
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
