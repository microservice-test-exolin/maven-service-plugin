package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.Layout;

/**
 *
 * @author tomgk
 */
public class ServiceConfigServlet extends HttpServlet
{
    public static final String URL = "/services/config";
    
    private final Services services;

    public ServiceConfigServlet(Services services)
    {
        this.services = services;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
            return;
        }
        
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Service service = services.getService(serviceName);
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service not found");
                return;
            }
            
            Layout.start("Config of "+serviceName, req.getRequestURI(), out);
            
            out.append("<h1>Config of "+serviceName+"</h1>");
            
            //out.append("</div>");
            Layout.end(out);
        }
    }
}
