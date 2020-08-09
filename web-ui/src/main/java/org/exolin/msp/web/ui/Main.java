package org.exolin.msp.web.ui;

import java.nio.file.Paths;
import java.util.Arrays;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.web.ui.servlet.ListServicesServlet;
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
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});
        
        SystemAbstraction sys = new PseudoAbstraction(new Log(){
            @Override
            public void warn(String string)
            {
            }

            @Override
            public void info(String string)
            {
            }
        });
        
        /*Services services = new StubServices(Arrays.asList(
                new StubService("mittens-discord", sys),
                new StubService("milkboi-discord", sys),
                new StubService("milkboi-telegram", sys)
        ));*/
        Services services = new LinuxServices(Paths.get("/home/exolin/services"), sys);
        
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/services").setServlet(new ListServicesServlet(services));
        
        server.setHandler(servletHandler);
        
        server.start();
    }
}
