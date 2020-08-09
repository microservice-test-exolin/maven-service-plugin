package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Map<String, Exception> exceptions = new HashMap<>();
            Map<String, String> statusMap = new HashMap<>();
            
            out.append("<html>");
            out.append("<head>");
            out.append("<title>Services</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");
            
            out.append("<h1>Service Web UI</h1>");
            
            out.append("<a href=\"/services\">Services</a><br>");
            out.append("<a href=\"/processes\">Processes</a><br>");
                        
            out.append("</div>");
            out.append("</body>");
            out.append("</html>");
        }
    }
}
