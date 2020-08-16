package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.Layout;
import static org.exolin.msp.web.ui.servlet.service.ListServicesServlet.write;

/**
 *
 * @author tomgk
 */
public class ServiceServlet extends HttpServlet
{
    private final Services services;
    
    public static final String URL = "/service";

    public ServiceServlet(Services services)
    {
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Missing parameter service");
            return;
        }
        
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Map<String, Exception> exceptions = new HashMap<>();
            
            Service service = services.getService(serviceName);
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Service not found");
                return;
            }
            
            Layout.start("Service "+serviceName, req.getRequestURI(), out);
            
            out.append("<h1>Service "+serviceName+"</h1>");
            
            out.append("<div class=\"card\">");
            out.append("<div class=\"card-header\">Service</div>\n");
            
            out.append("<table class=\"table table-sm\">");
            
            out.append("<tr>");
            out.append("<th>Name</th>");
            out.append("<td>").append(service.getName()).append("</td>");
            out.append("</tr>");
            
            out.append("<tr>");
            out.append("<th>Status</th>");
            out.append("<td>");
            try{
                StatusInfo status = service.getStatus();
                try{
                    StatusType statusType = status.getStatus();

                    out.append(statusType.toString());

                }catch(UnsupportedOperationException e){
                    exceptions.put(service.getName(), e);
                    out.append("Couldn't be determined");
                }
            }catch(IOException e){
                exceptions.put(service.getName(), e);
                out.append("Couldn't be determined");
            }
            out.append("</td>");
            out.append("</tr>");
            
            out.append("<tr>");
            out.append("<th>Links</th>");
            out.append("<td>");
            out.append("<a href=\""+ServiceStatusServlet.getUrl(service.getName())+"\">Status</a>");
            out.append("<br>");
            out.append("<a href=\""+LogServlet.getFilesOfService(service.getName())+"\">Service Logfiles</a><br>");
            out.append("</td>");
            out.append("</tr>");
            out.append("</table>");
            
            out.append("<div class=\"card-body\">");
            out.append("<form action=\""+ListServicesServlet.URL+"\" method=\"POST\" style=\"display: inline\">");
            out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
            write(out, "start", "Start");
            write(out, "stop", "Stop");
            write(out, "restart", "Restart");
            out.append("</form>");
            
            out.append("</div></div>");
            
            out.append("<br>");
            
            out.append("<div class=\"card\">");
            out.append("<div class=\"card-header\">Build/Deployment</div>\n");
            out.append("<div class=\"card-body\">");
            out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "build")+"\">Build Logfiles</a><br>");
            out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "deploy")+"\">Deploy Logfiles</a><br>");
            if(true || service.supportsBuildAndDeployment())
            {
                out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
                if(!service.isBuildOrDeployProcessRunning())
                {
                    out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                    write(out, "compile", "Compile");
                    write(out, "deploy", "Deploy");
                    out.append("</form>");
                }
                else
                    out.append("Build/deploy currently running");
            }
            out.append("</div></div>");
            
            if(!exceptions.isEmpty())
            {
                out.append("<h2>Errors</h2>");
                out.append("<table class=\"table table-striped table-sm\">");
                for(Map.Entry<String, Exception> e: exceptions.entrySet())
                {
                    out.append("<tr>");
                    out.append("<td>");
                    out.append(e.getKey());
                    out.append("</td>");
                    out.append("<td><pre>");
                    e.getValue().printStackTrace(out);
                    out.append("</pre></td>");
                    out.append("</tr>");
                }
                out.append("</table>");
            }
            
            //out.append("</div>");
            Layout.end(out);
        }
    }
}
