package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;

/**
 *
 * @author tomgk
 */
public class ListServicesServlet extends HttpServlet
{
    private final Services services;

    public ListServicesServlet(Services services)
    {
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(Writer out = resp.getWriter())
        {
            out.append("<html>");
            out.append("<head>");
            out.append("<title>Services</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");
            
            out.append("<h1>Services</h1>");
            
            out.append("<table class=\"table\">");
            
            out.append("<tr>");
            out.append("<th>Name</th>");
            out.append("<th>Status</th>");
            out.append("<th></th>");
            out.append("</tr>");
            
            for(Service service: services.getServices())
            {
                out.append("<tr>");
                out.append("<td>").append(service.getName()).append("</td>");
                out.append("<td>").append(service.getStatus()).append("</td>");
                
                out.append("<td>");
                out.append("<form action=\"#\" method=\"POST\">");
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "start", "Start");
                write(out, "stop", "Stop");
                write(out, "restart", "Restart");
                out.append("</form>");
                out.append("</td>");
                
                out.append("</tr>");
            }
            
            out.append("</table>");
            out.append("</div>");
            
            out.append("</body>");
            out.append("</html>");
        }
    }
    
    private void write(Writer out, String action, String title) throws IOException
    {
        out.append("<button name=\"action\" value=\""+action+"\" class=\"btn btn-secondary\">").append(title).append("</button> ");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
            return;
        }
        
        String action = req.getParameter("action");
        if(action == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter action");
            return;
        }
        
        Service service = services.getServices(serviceName);
        if(service == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
            return;
        }
        
        switch(action)
        {
            case "start": service.start(); break;
            case "stop": service.stop(); break;
            case "restart": service.restart(); break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
        }
        
        resp.sendRedirect(req.getRequestURI());  //back to GET
    }
}