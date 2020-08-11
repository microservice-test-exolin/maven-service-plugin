package org.exolin.msp.web.ui.servlet.service;

import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.servlet.Fame;

/**
 *
 * @author tomgk
 */
public class LogServlet extends HttpServlet
{
    private final Services services;

    public LogServlet(Services services)
    {
        this.services = services;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            showLogFileIndex(services, req, resp);//resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
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
        
        String logFile = req.getParameter("logfile");
        if(logFile == null)
            showLogFileIndex(service, req, resp);
        else if("raw".equals(req.getParameter("type")))
            showLogFileRaw(service, logFile, req, resp);
        else
            showLogFile(service, logFile, req, resp);
    }

    static String getUrl(String service, String logfile)
    {
        return getUrl(service, logfile, false);
    }
    
    static String getUrl(String service, String logfile, boolean raw)
    {
        return "/logs?service="+service+"&logfile="+logfile+(raw ? "&type=raw" : "");
    }
    
    private void showLogFileIndex(Services services, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Fame.start("Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Logfiles</h1>");
            
            for(Service service: services.getServices())
            {
                for(String name: service.getLogFiles().keySet())
                {
                    out.append("<a href=\"").append(ListServicesServlet.getUrl(service.getName())).append("\">").append(service.getName()).append("</a>");
                    out.append(" / <a href=\"").append(getUrl(service.getName(), name)).append("\">").append(name).append("</a><br>");
                }
            }
            
            Fame.end(out);
        }
    }
    
    private void showLogFileIndex(Service service, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Fame.start("Logfiles of "+service.getName(), req.getRequestURI(), out);
            
            /*out.append("<html>");
            out.append("<head>");
            out.append("<title>Logfiles of "+service.getName()+"</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");*/
            
            out.append("<h1>Logfiles of "+service.getName()+"</h1>");
            
            for(String name: service.getLogFiles().keySet())
                out.append("<a href=\""+getUrl(service.getName(), name)+"\">"+name+"</a><br>");
            
            /*out.append("</body>");
            out.append("</head>");
            out.append("</html>");*/
            Fame.end(out);
        }
    }
    
    private void showLogFileRaw(Service service, String logFile, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/plain;charset=UTF-8");
        Path path = service.getLogFiles().get(logFile);
        if(path == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Logfile "+logFile+" not found");
            return;
        }
        
        Files.copy(path, resp.getOutputStream());
    }
    
    private void showLogFile(Service service, String logFile, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        Path path = service.getLogFiles().get(logFile);
        if(path == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Logfile "+logFile+" not found");
            return;
        }
        
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Fame.start("Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Logfiles</h1>");
            
            out.append("<a href=\"").append(getUrl(service.getName(), logFile, true)).append("\">").append("Raw").append("</a>");
            
            out.append("<pre style=\"border: 1px solid #ccc;padding:0.5em\">");
            
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            Files.copy(path, arr);
            out.append(arr.toString().replace("<", "&lt;").replace(">", "&gt;"));
            
            out.append("</pre>");
            
            Fame.end(out);
        }
    }
}
