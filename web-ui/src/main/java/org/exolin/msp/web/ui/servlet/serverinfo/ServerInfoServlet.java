package org.exolin.msp.web.ui.servlet.serverinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.servlet.Layout;

/**
 *
 * @author tomgk
 */
public class ServerInfoServlet extends HttpServlet
{
    public static final String URL = "/server-info";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        
        Layout.start("Server info", req.getRequestURI(), out);
        
        out.append("<h1>Server info</h1>");
        
        out.append("<a href=\""+SystemPropertiesServlet.URL+"\">System properties</a><br>");
        out.append("<a href=\""+SystemEnvironmentServlet.URL+"\">Environment Variables</a>");
        
        Layout.end(out);
    }
    
    static void list(Writer out, Map<String, String> tab) throws IOException
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
