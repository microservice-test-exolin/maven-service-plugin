package org.exolin.msp.web.ui.servlet.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.log.LogLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class TaskLogServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLogServlet.class);
    
    private final Services services;

    public static final String URL = "/tasks/logs";

    public TaskLogServlet(Services services)
    {
        this.services = services;
    }
    
    private static final String SERVICE = "service";
    private static final String TASK = "task";
    
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

            String task = req.getParameter(TASK);
            if(task == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter task");
                return;
            }
            
            LogLister.listServiceFiles(service, Optional.of(task), req, resp);
        }catch(IOException|RuntimeException e){
            LOGGER.error("Error", e);
            throw e;
        }
    }
    
    public static String getFilesOfTask(String service, String taskName)
    {
        return URL+"?"+SERVICE+"="+service+"&"+TASK+"="+taskName;
    }
    
    private void listServices(Services services, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Task Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Task Logfiles</h1>");
            
            for(Service service: services.getServices())
            {
                for(String task: service.getTasks())
                {
                    out.append("<a href=\"").append(getFilesOfTask(service.getName(), task)).append("\">");
                    Icon.SERVICE.writeTo(out);
                    out.append(service.getName()).append(" - Task ").append(task).append("</a><br>");
                }
            }
            
            Layout.end(out);
        }
    }
}
