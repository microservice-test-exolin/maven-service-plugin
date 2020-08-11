package org.exolin.msp.web.ui.servlet.serverinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.servlet.Layout;
import static org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet.list;
import static org.exolin.msp.web.ui.servlet.serverinfo.ServerInfoServlet.path;

/**
 *
 * @author tomgk
 */
public class SystemPropertiesServlet extends HttpServlet
{
    public static final String URL = "/server-info/system-properties";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        
        Layout.start("System properties", req.getRequestURI(), out);
        
        out.append("<h1><a href=\""+ServerInfoServlet.URL+"\">System info</a> / System properties</h1>");
        list(out, (Map)System.getProperties(), SystemPropertiesServlet::format);
        
        Layout.end(out);
    }
    
    private static String format(String key, String value)
    {
        if(key.endsWith(".path") || key.equals("java.ext.dirs"))
        //if(key.equals("java.class.path") || key.equals("java.library.path") || key.equals("sun.boot.class.path"))
            return path(value);
        
        else
            return value;
    }
}
