package org.exolin.msp.web.ui;

import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.linux.LinuxServices;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.exolin.msp.web.ui.servlet.FaviconIcoServlet;
import org.exolin.msp.web.ui.servlet.FaviconPngServlet;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogServlet;
import org.exolin.msp.web.ui.servlet.ProcessServlet;
import org.exolin.msp.web.ui.servlet.ResourceServlet;
import org.exolin.msp.web.ui.servlet.ServerInfoClass;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.stub.StubService;
import org.exolin.msp.web.ui.stub.StubServices;

/**
 *
 * @author tomgk
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        run(false);
    }
    
    public static void run(boolean testEnv) throws Exception
    {
        Log log = new Log(){
                @Override
                public void warn(String string)
                {
                    System.out.println("[WARN] "+string);
                }

                @Override
                public void info(String string)
                {
                    System.out.println("[INFO] "+string);
                }
            };
        
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});
        
        SystemAbstraction sys;
        Services services;
        ProcessManager pm = new ProcessManager();
        
        Runtime.getRuntime().addShutdownHook(new Thread("childprocess.kill"){
            @Override
            public void run()
            {
                pm.killAll();
            }
        });
        
        if(testEnv)
        {
            sys = new PseudoAbstraction(log);

            services = new StubServices(Arrays.asList(
                    new StubService("test-mittens-discord", sys),
                    new StubService("test-milkboi-discord", sys),
                    new StubService("test-milkboi-telegram", sys)
            ));
            
            Files.write(Paths.get("test.log"), Arrays.asList("Log Entry"));
        }
        else
        {
            sys = new LinuxAbstraction(log);
            
            services = new LinuxServices(Paths.get("/home/exolin/services"), sys);
        }
        
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
        
        servletHandler.addServletWithMapping(ServerInfoClass.class, "/server-info");
        
        /*ResourceHandler resHandler = new ResourceHandler();
        resHandler.setBaseResource(Resource.newClassPathResource("/"));
        resHandler.setDirectoriesListed(false);
        resHandler.setDirAllowed(false);
        HandlerCollection col = new HandlerCollection();
        col.addHandler(servletHandler);
        col.addHandler(resHandler);*/
        server.setHandler(servletHandler);
        
        server.start();
    }
}
