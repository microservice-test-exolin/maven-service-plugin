package org.exolin.msp.web.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessDataStorage;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.service.stub.StubService;
import org.exolin.msp.service.stub.StubServices;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tomgk
 */
public class MainTest
{
    private static Server server;
    private static int port;
    
    @BeforeAll
    public static void setup() throws Exception
    {
        ProcessManager pm = new ProcessManager(new ProcessDataStorage(Paths.get(".")));
        
        SystemAbstraction sys = new PseudoAbstraction(new LogAdapter(PseudoAbstraction.class));
        Services services = new StubServices(Arrays.asList(
                    new StubService("test-mittens-discord", sys, Collections.emptyMap()),
                    new StubService("test-milkboi-discord", sys, Collections.emptyMap()),
                    new StubService("test-milkboi-telegram", sys, Collections.emptyMap())
            ));
        
        Properties properties = new Properties();
        properties.setProperty(Config.KEY_AUTH_TYPE, Config.AuthType.none.name());
        server = Main.create(pm, sys, services, new Config(properties), 0);
        server.start();
        
        port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }
    
    @AfterAll
    public static void teardown() throws Exception
    {
        server.stop();
    }
    
    private HttpURLConnection open(String path) throws MalformedURLException, IOException
    {
        return ((HttpURLConnection)new URL("http://localhost:"+port+"/"+path).openConnection());
    }
    
    @Test
    public void testAccessible() throws Exception
    {
        assertEquals(200, open("").getResponseCode());
    }
    
    @Test
    public void testWrongPath() throws Exception
    {
        assertEquals(404, open("unmapped").getResponseCode());
    }
    
    @Test
    public void testResources() throws Exception
    {
        assertEquals("image/png", open("favicon.png").getContentType());
        assertEquals("image/x-icon", open("favicon.ico").getContentType());
        assertEquals("text/css", open("dashboard.css").getContentType());
    }
}
