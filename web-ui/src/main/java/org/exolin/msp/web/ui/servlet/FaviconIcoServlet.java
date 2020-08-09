package org.exolin.msp.web.ui.servlet;

/**
 *
 * @author tomgk
 */
public class FaviconIcoServlet extends ResourceServlet
{
    public FaviconIcoServlet()
    {
        super("image/x-icon", "files/favicon.ico");
    }
}
