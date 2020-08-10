package org.exolin.msp.web.ui;

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
import org.exolin.msp.web.ui.servlet.DeployServlet;
import org.exolin.msp.web.ui.servlet.FaviconIcoServlet;
import org.exolin.msp.web.ui.servlet.FaviconPngServlet;
import org.exolin.msp.web.ui.servlet.IndexServlet;
import org.exolin.msp.web.ui.servlet.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.LogServlet;
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
        
        boolean testEnv = true;
        
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
                    new StubService("mittens-discord", sys),
                    new StubService("milkboi-discord", sys),
                    new StubService("milkboi-telegram", sys)
            ));
            
            Files.write(Paths.get("test.log"), Arrays.asList("Log Entry"));
        }
        else
        {
            sys = new LinuxAbstraction(log);
            
            services = new LinuxServices(Paths.get("/home/exolin/services"), sys);
        }
        
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        servletHandler.addServletWithMapping(IndexServlet.class, "/");
        servletHandler.addServletWithMapping(FaviconPngServlet.class, "/favicon.png");
        servletHandler.addServletWithMapping(FaviconIcoServlet.class, "/favicon.ico");
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
