package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class IndexServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexServlet.class);
    
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
            /*out.append("<html>");
            out.append("<head>");
            out.append("<title>Services</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");*/
            Layout.start("Service Web UI", req.getRequestURI(), out);
            
            out.append("<h1>Service Web UI</h1>");
            
            out.append("<p><a href=\"/services\"><span data-feather=\""+Layout.SERVICE+"\"></span> Services</a></p>");
            out.append("<p><a href=\"/processes\"><span data-feather=\""+Layout.PROCESS+"\"></span> Processes</a></p>");
                        
            /*out.append("</div>");
            out.append("</body>");
            out.append("</html>");*/
            Layout.end(out);
        }
    }
}
