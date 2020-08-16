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
import org.exolin.msp.web.ui.servlet.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LogServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogServlet.class);
    
    private final Services services;

    public static final String URL = "/logs";

    public LogServlet(Services services)
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
                listServices(services, req, resp);//resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
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
            listServiceFiles(service, Optional.ofNullable(task), req, resp);
        }catch(IOException|RuntimeException e){
            LOGGER.error("Error", e);
            throw e;
        }
    }
    
    static String getFilesOfService(String service)
    {
        return URL+"?"+SERVICE+"="+service;
    }
    
    static String getFilesOfTask(String service, String taskName)
    {
        return URL+"?"+SERVICE+"="+service+"&"+TASK+"="+taskName;
    }
    
    private void listServices(Services services, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Logfiles</h1>");
            
            for(Service service: services.getServices())
            {
                out.append("<a href=\"").append(getFilesOfService(service.getName())).append("\">");
                out.append("<span data-feather=\""+Layout.SERVICE+"\"></span> ");
                out.append(service.getName()).append("</a><br>");
            }
            
            Layout.end(out);
        }
    }
    
    private void listServiceFiles(Service service, Optional<String> taskName, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Logfiles"+taskName.map(t -> " "+t).orElse("")+" of "+service.getName(), req.getRequestURI(), out);
            
            out.append("<h1>Logfiles"+taskName.map(t -> " "+t).orElse("")+" of "+service.getName()+"</h1>");
            
            Map<String, LogFile> files = service.getLogFiles(taskName);
            files.forEach((name, lf) -> {
                out.append("<a href=\""+LogFileShowServlet.getFileUrl(service.getName(), name)+"\">");
                out.append("<span data-feather=\""+Layout.LOG+"\"></span> ");
                out.append(LognameGenerator.getLogFileTitle(lf)+"</a><br>");
            });
            if(files.isEmpty())
                out.append("<em>No files found</em>");
            
            Layout.end(out);
        }
    }
}
