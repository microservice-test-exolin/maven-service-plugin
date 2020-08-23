package org.exolin.msp.web.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
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
        
        List<String> names = Arrays.asList(
                "test-mittens-discord",
                "test-milkboi-discord",
                "test-milkboi-telegram"
        );
        
        Path root = Paths.get("repos");
        String prefix = "http://github.test/a/";
        Services services = new StubServices(names.stream().map(name -> new StubService(
                name,
                root.resolve(name),
                root.resolve(name).resolve("x"),
                prefix+name,
                sys,
                Collections.emptyMap()
        )).collect(Collectors.toList()));
        
        Properties properties = new Properties();
        properties.setProperty(Config.KEY_AUTH_TYPE, Config.AuthType.none.name());
        server = Main.create(pm, sys, services, new Config(properties), 0, true);
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
    public void testAccessibleWithNonLocalhost() throws Exception
    {
        InetAddress nonLocalhostAddress = Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
                .stream()
                .flatMap(inf -> Collections.list(inf.getInetAddresses()).stream())
                .filter(f -> !f.isLoopbackAddress())
                .filter(f -> f instanceof Inet4Address)
                .findFirst().get();
        
        HttpURLConnection con = (HttpURLConnection)new URL("http://"+nonLocalhostAddress.getHostAddress()+":"+port+"/").openConnection();
        
        try{
            con.connect();
            fail("Accessible over "+nonLocalhostAddress.getHostAddress());
        }catch(ConnectException e){
            assertTrue(e.getMessage().startsWith("Connection refused"));
        }
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
