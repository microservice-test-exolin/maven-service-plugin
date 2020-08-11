package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.servlet.Layout;

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
        
        try(PrintWriter out = resp.getWriter())
        {
            Map<String, Exception> exceptions = new HashMap<>();
            Map<String, String> statusMap = new HashMap<>();
            
            Layout.start("Services", req.getRequestURI(), out);
            //out.append("<div class=\"d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom\">");
            
            out.append("<h1>Services</h1>");
            
            out.append("<table class=\"table table-striped table-sm\">");
            
            out.append("<tr>");
            out.append("<th>Name</th>");
            out.append("<th>Status</th>");
            out.append("<th colspan=\"3\"></th>");
            out.append("</tr>");
            
            for(Service service: services.getServices())
            {
                out.append("<tr id=\"").append(service.getName()).append("\">");
                out.append("<td>").append(service.getName()).append("</td>");
                
                out.append("<td>");
                statusMap.put(service.getName(), "failed to read");
                try{
                    StatusInfo status = service.getStatus();
                    try{
                        StatusType statusType = status.getStatus();
                        
                        out.append(statusType.toString());
                        
                        statusMap.put(service.getName(), status.getInfo());
                    }catch(UnsupportedOperationException e){
                        exceptions.put(service.getName(), e);
                        out.append("Couldn't be determined");
                    }
                }catch(IOException e){
                    exceptions.put(service.getName(), e);
                    out.append("Couldn't be determined");
                }
                out.append("</td>");
                
                out.append("<td>");
                out.append("<form action=\"#\" method=\"POST\" style=\"display: inline\">");
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "start", "Start");
                write(out, "stop", "Stop");
                write(out, "restart", "Restart");
                out.append("</form>");
                out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
                
                out.append("</td>");
                
                out.append("<td>");
                if(service.supportsBuildAndDeployment())
                {
                    out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                    write(out, "compile", "Compile");
                    write(out, "deploy", "Deploy");
                    out.append("</form>");
                }
                out.append("</td>");
                
                out.append("<td>");
                out.append("<a href=\"/logs?service="+service.getName()+"\">Logfiles</a><br>");
                
                out.append("</td>");
                
                out.append("</tr>");
            }
            
            out.append("</table>");
            
            out.append("<h2>Status</h2>");
            out.append("<table class=\"table table-striped table-sm\">");out.append("<tr>");
            out.append("<th>Service</th>");
            out.append("<th>Status</th>");
            out.append("</tr>");
            for(Map.Entry<String, String> e: statusMap.entrySet())
            {
                out.append("<tr>");
                out.append("<td>");
                out.append(e.getKey());
                out.append("</td>");
                out.append("<td><pre>");
                out.append(e.getValue());
                out.append("</pre></td>");
                out.append("</tr>");
            }
            out.append("</table>");
            
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

    public static String getUrl(String service)
    {
        return "/services#"+service;
    }
    
    static void write(Writer out, String action, String title) throws IOException
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