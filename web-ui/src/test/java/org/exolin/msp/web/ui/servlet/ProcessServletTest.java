package org.exolin.msp.web.ui.servlet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tomgk
 */
public class ProcessServletTest
{
    @Test
    public void testDisplayInitiator_Webhook_WithSha1()
    {
        assertEquals(
                "<a href=\"https://github.com/microservice-test-exolin/maven-service-plugin/commit/2d647bd3a8beacf68b0230e9f476ce27be04e3d2\">"+
                "Github Webhook</a>",
                ProcessServlet.displayInitiator("github-webhook[repo=https://github.com/microservice-test-exolin/maven-service-plugin,sha1=2d647bd3a8beacf68b0230e9f476ce27be04e3d2]"));
    }
    
    @Test
    public void testDisplayInitiator_Webhook_WithNoSha1()
    {
        assertEquals(
                "<a href=\"https://github.com/microservice-test-exolin/maven-service-plugin\">"+
                "Github Webhook</a>",
                ProcessServlet.displayInitiator("github-webhook[repo=https://github.com/microservice-test-exolin/maven-service-plugin]"));
    }
    
    
    @Test
    public void testDisplayInitiator_Webhook_WithNothing()
    {
        assertEquals(
                "Github Webhook",
                ProcessServlet.displayInitiator("github-webhook"));
    }
    
    @Test
    public void testDisplayInitiator_ServiceWebUI_WithoutUser()
    {
        assertEquals(
                "Service Web UI",
                ProcessServlet.displayInitiator("service-web-ui"));
    }
    
    @Test
    public void testDisplayInitiator_ServiceWebUI_WithUser()
    {
        assertEquals("Service Web UI by sample", ProcessServlet.displayInitiator("service-web-ui[user=sample]"));
    }
}
