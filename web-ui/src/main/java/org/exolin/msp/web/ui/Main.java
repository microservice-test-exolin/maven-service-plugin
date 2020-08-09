package org.exolin.msp.web.ui;

import org.exolin.msp.web.ui.servlet.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.StatusServlet;
import org.exolin.msp.web.ui.stub.StubServices;
import org.exolin.msp.web.ui.stub.StubService;
import java.util.Arrays;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

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
        
        Services services = new StubServices(Arrays.asList(
                new StubService("mittens-discord"),
                new StubService("milkboi-discord"),
                new StubService("milkboi-telegram")
        ));
        
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        servletHandler.addServletWithMapping(ListServicesServlet.class, "/services").setServlet(new ListServicesServlet(services));
        
        server.setHandler(servletHandler);
        
        server.start();
    }
}
