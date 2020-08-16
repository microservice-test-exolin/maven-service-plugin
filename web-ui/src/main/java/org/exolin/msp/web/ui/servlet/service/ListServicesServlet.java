package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ListServicesServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ListServicesServlet.class);
    
    private final Services services;
    
    public static final String URL = "/services";

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
                out.append("<td><a href=\"").append(getUrl(service.getName())).append("\">").append(service.getName()).append("</a></td>");
                
                out.append("<td>");
                try{
                    ServiceServlet.writeStatus(out, service.getStatus());
                }catch(IOException e){
                    LOGGER.error("Couldn't be determined", e);
                    out.append("Couldn't be determined");
                }
                out.append("</td>");
                
                out.append("<td>");
                out.append("<form action=\"#\" method=\"POST\" style=\"display: inline\">");
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "start", Layout.START, "Start");
                write(out, "stop", Layout.STOP, "Stop");
                write(out, "restart", Layout.RESTART, "Restart");
                out.append("</form>");
                
                out.append("<a href=\""+ServiceStatusServlet.getUrl(service.getName())+"\">Status</a>");
                
                out.append("</td>");
                
                out.append("<td>");
                if(service.supportsBuildAndDeployment())
                {
                    out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
                    if(!service.isBuildOrDeployProcessRunning())
                    {
                        out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                        write(out, "compile", Layout.COMPILE, "Compile");
                        write(out, "deploy", Layout.DEPLOY, "Deploy");
                        out.append("</form>");
                    }
                    else
                        out.append("Build/deploy currently running");
                }
                out.append("</td>");
                
                out.append("<td>");
                out.append("<a href=\""+LogServlet.getFilesOfService(service.getName())+"\">Service Logfiles</a><br>");
                out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "build")+"\">Build Logfiles</a><br>");
                out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "deploy")+"\">Deploy Logfiles</a><br>");
                
                out.append("</td>");
                
                out.append("</tr>");
            }
            
            out.append("</table>");
            
            //out.append("</div>");
            Layout.end(out);
        }
    }

    public static String getUrl(String service)
    {
        return "/service?service="+service;
    }
    
    static void write(Writer out, String action, String icon, String title) throws IOException
    {
        out.append("<button name=\"action\" value=\""+action+"\" class=\"btn btn-secondary\">");
        out.append("<span data-feather=\""+icon+"\"></span> ");
        out.append(title).append("</button> ");
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
