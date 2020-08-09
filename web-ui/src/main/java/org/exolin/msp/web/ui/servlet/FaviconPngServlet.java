package org.exolin.msp.web.ui.servlet;

/**
 *
 * @author tomgk
 */
public class FaviconPngServlet extends ResourceServlet
{
    public FaviconPngServlet()
    {
        super("image/png", "files/favicon.png");
    }
}
