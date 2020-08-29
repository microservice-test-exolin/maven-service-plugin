package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tomgk
 */
public class UnsupportedServlet extends HttpServlet
{
    private final String message;

    public UnsupportedServlet(String message)
    {
        this.message = message;
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, message);
    }
}
