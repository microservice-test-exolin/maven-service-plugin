package org.exolin.msp.web.ui.servlet.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessDataStorage;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.service.stub.StubService;
import org.exolin.msp.service.stub.StubServices;
import org.exolin.msp.web.ui.Config;
import org.exolin.msp.web.ui.LogAdapter;
import org.exolin.msp.web.ui.Main;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author tomgk
 */
public class AuthFilterTest
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
        properties.setProperty(Config.KEY_AUTH_TYPE, Config.AuthType.github.name());
        properties.setProperty(Config.KEY_GITHUB_CLIENT_ID, "fake");
        properties.setProperty(Config.KEY_GITHUB_CLIENT_SECRET, "fake");
        properties.setProperty(Config.ALLOWED_USERS, "someone");
        server = Main.create(pm, sys, services, new Config(properties), Paths.get("invalid"), 0, true);
        server.start();
        
        port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }
    
    private HttpURLConnection open(String path) throws MalformedURLException, IOException
    {
        return ((HttpURLConnection)new URL("http://localhost:"+port+path).openConnection());
    }
    
    private void assertRedirect(String path) throws IOException
    {
        HttpURLConnection con = open(path);
        assertEquals(302, con.getResponseCode());
        assertEquals("https://github.com/login/oauth/authorize?client_id=fake", con.getHeaderField("Location"));
    }
    
    public static Stream<String> paths()
    {
        return Stream.concat(Stream.of("/"), Layout.menus.stream().flatMap(m -> m.getItems().stream()).map(mi -> mi.getLink()));
    }
    
    @DisplayName("Check not accessible")
    @ParameterizedTest(name = "Path {0}")
    @MethodSource("paths")
    public void testNotAccessible(String path) throws Exception
    {
        assertRedirect(path);
    }
    
    @Test
    public void testGithubWebhookAccessible() throws Exception
    {
        assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, open(GithubWebhookServlet.URL).getResponseCode());
    }
}
