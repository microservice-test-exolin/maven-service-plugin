package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tomgk
 */
public class IndexServlet extends HttpServlet
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if(req.getPathInfo() != null && !req.getPathInfo().equals("/"))
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        super.service(req, resp);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Service Web UI", req.getRequestURI(), out);
            
            out.append("<h1>Service Web UI</h1>");
            
            out.append("<p><a href=\"/services\">");
            Icon.SERVICE.writeTo(out);
            out.append(" Services</a></p>");
            
            out.append("<p><a href=\"/processes\">");
            Icon.PROCESS.writeTo(out);
            out.append(" Processes</a></p>");
            
            Layout.end(out);
        }
    }
}
