package org.exolin.msp.web.ui.servlet.task;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests für {@link InitiatorFormatter}.
 * 
 * @author tomgk
 */
public class InitiatorFormatterTest
{
    @Test
    public void testDisplayInitiator_Webhook_WithSha1()
    {
        assertEquals(
                "<a href=\"https://github.com/microservice-test-exolin/maven-service-plugin/commit/2d647bd3a8beacf68b0230e9f476ce27be04e3d2\"" +
                " title=\"Github Webhook - Commit 2d647bd\"" +
                ">"+
                "Commit 2d647bd</a>",
                InitiatorFormatter.displayInitiator("github-webhook[repo=https://github.com/microservice-test-exolin/maven-service-plugin,sha1=2d647bd3a8beacf68b0230e9f476ce27be04e3d2]"));
    }
    
    @Test
    public void testDisplayInitiator_Webhook_WithNoSha1()
    {
        assertEquals(
                "<a href=\"https://github.com/microservice-test-exolin/maven-service-plugin\">"+
                "Github Webhook</a>",
                InitiatorFormatter.displayInitiator("github-webhook[repo=https://github.com/microservice-test-exolin/maven-service-plugin]"));
    }
    
    @Test
    public void testDisplayInitiator_Webhook_WithNothing()
    {
        assertEquals(
                "Github Webhook",
                InitiatorFormatter.displayInitiator("github-webhook"));
    }
    
    @Test
    public void testDisplayInitiator_ServiceWebUI_WithoutUser()
    {
        assertEquals(
                "Service Web UI",
                InitiatorFormatter.displayInitiator("service-web-ui"));
    }
    
    @Test
    public void testDisplayInitiator_ServiceWebUI_WithUser()
    {
        assertEquals("Service Web UI by sample", InitiatorFormatter.displayInitiator("service-web-ui[user=sample]"));
    }
}
