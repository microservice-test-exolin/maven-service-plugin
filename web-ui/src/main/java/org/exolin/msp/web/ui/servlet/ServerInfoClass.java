package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tomgk
 */
public class ServerInfoClass extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        
        Fame.start("Server Info", req.getRequestURI(), out);
        
        out.append("<h1>Server Info</h1>");
        
        out.append("<h2>System properties</h2>");
        list(out, (Map)System.getProperties());
        
        out.append("<h2>Environment Variables</h2>");
        list(out, System.getenv());
        
        Fame.end(out);
    }
    
    private void list(Writer out, Map<String, String> tab) throws IOException
    {
        out.append("<table class=\"table table-striped table-sm\">");
        for(Map.Entry<String, String> p: tab.entrySet())
        {
            out.append("<tr>");
            out.append("<td>").append(p.getKey()).append("</td>");
            out.append("<td>").append(p.getValue()).append("</td>");
            out.append("</tr>");
        }
        out.append("</table>");
    }
}
