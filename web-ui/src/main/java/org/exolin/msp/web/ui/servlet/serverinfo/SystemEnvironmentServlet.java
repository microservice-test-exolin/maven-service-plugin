package org.exolin.msp.web.ui.servlet.serverinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.servlet.Fame;
import static org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet.list;

/**
 *
 * @author tomgk
 */
public class SystemEnvironmentServlet extends HttpServlet
{
    public static final String URL = "/server-info/environment";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        
        Fame.start("Server Info", req.getRequestURI(), out);
        
        out.append("<h1><a href=\""+ServerInfoServlet.URL+"\">System info</a> / Environment Variables</h1>");
        list(out, System.getenv());
        
        Fame.end(out);
    }
    
}
