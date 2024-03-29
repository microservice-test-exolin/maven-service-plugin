package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ServiceStatusServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStatusServlet.class);
    
    private final Services services;

    public ServiceStatusServlet(Services services)
    {
        this.services = services;
    }
    
    public static final String URL = "/service/status";
    
    public static String getUrl(String serviceName)
    {
        return URL+"?service="+serviceName;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            String serviceName = HttpUtils.getRequiredParameter(req, "service");
        
            Service service = services.getService(serviceName);
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
                return;
            }
            
            String title = "Status of service "+service.getName();
            
            Layout.start(title, req.getRequestURI(), out);
            
            out.append("<h1>").append(title).append("</h1>");
            
            StatusInfo status;
            
            try{
                status = service.getApplicationInstance().getStatus();
            }catch(IOException e){
                out.append("Couldn't read status");
                LOGGER.error("Couldn't read status", e);
                status = null;
            }
            
            if(status != null)
            {
                out.append("Status: ");
                ServiceServlet.writeStatus(out, status);
                out.append("<br>");

                out.append("<pre>");
                out.append(status.getInfo());
                out.append("</pre>");

                out.append("</table>");
            }
            
            Layout.end(out);
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
