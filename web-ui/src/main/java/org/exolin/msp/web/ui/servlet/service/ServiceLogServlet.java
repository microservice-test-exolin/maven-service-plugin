package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.LognameGenerator;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.log.LogLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ServiceLogServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLogServlet.class);
    
    private final Services services;

    public static final String URL = "/logs/services";

    public ServiceLogServlet(Services services)
    {
        this.services = services;
    }
    
    private static final String SERVICE = "service";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            String serviceName = req.getParameter("service");
            if(serviceName == null)
            {
                listServices(services, req, resp);
                return;
            }

            Service service;
            try{
                service = services.getService(serviceName);
            }catch(IOException e){
                throw new ServletException(e);
            }
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
                return;
            }

            LogLister.listServiceFiles(service, Optional.empty(), req, resp);
        }catch(IOException|RuntimeException e){
            LOGGER.error("Error", e);
            throw e;
        }
    }
    
    static String getFilesOfService(String service)
    {
        return URL+"?"+SERVICE+"="+service;
    }
    
    private void listServices(Services services, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Service Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Service Logfiles</h1>");
            
            for(Service service: services.getServices())
            {
                out.append("<a href=\"").append(getFilesOfService(service.getName())).append("\">");
                Icon.SERVICE.writeTo(out);
                out.append(service.getName()).append("</a><br>");
            }
            
            Layout.end(out);
        }
    }
}
