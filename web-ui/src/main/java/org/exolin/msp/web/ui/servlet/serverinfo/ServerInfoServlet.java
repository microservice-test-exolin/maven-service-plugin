package org.exolin.msp.web.ui.servlet.serverinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.servlet.Icon;
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
        
        out.append("<a href=\""+SystemPropertiesServlet.URL+"\">");
        Icon.SERVER.writeTo(out);
        out.append("System properties</a><br>");
        
        out.append("<a href=\""+SystemEnvironmentServlet.URL+"\">");
        Icon.SERVER.writeTo(out);
        out.append("Environment Variables</a>");
        
        Layout.end(out);
    }
    
    static void list(Writer out, Map<String, String> tab) throws IOException
    {
        list(out, tab, (k, v) -> v);
    }
    
    static void list(Writer out, Map<String, String> tab, BiFunction<String, String, String> formatter) throws IOException
    {
        out.append("<table class=\"table table-striped table-sm\">");
        for(Map.Entry<String, String> p: tab.entrySet())
        {
            out.append("<tr>");
            out.append("<td>").append(p.getKey()).append("</td>");
            out.append("<td>").append(formatter.apply(p.getKey(), p.getValue())).append("</td>");
            out.append("</tr>");
        }
        out.append("</table>");
    }

    static String path(String value)
    {
        return String.join("<br>", value.split(Pattern.quote(System.getProperty("path.separator"))));
    }
}
