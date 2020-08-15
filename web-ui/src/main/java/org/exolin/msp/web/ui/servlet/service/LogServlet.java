package org.exolin.msp.web.ui.servlet.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.LognameGenerator;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.servlet.Layout;

/**
 *
 * @author tomgk
 */
public class LogServlet extends HttpServlet
{
    private final Services services;

    public static final String URL = "/logs";

    public LogServlet(Services services)
    {
        this.services = services;
    }
    
    private static final String SERVICE = "service";
    private static final String LOGFILE = "logfile";
    private static final String GROUP = "group";
    private static final String TYPE = "type";
    private static final String TYPE_RAW = "raw";
    
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
        
        String logFile = req.getParameter(LOGFILE);
        String group = req.getParameter(GROUP);
        if(logFile == null)
            showLogFileIndex(service, req, resp, group != null ? group : "");
        else if(TYPE_RAW.equals(req.getParameter(TYPE)))
            showLogFileRaw(service, logFile, req, resp);
        else
            showLogFile(service, logFile, req, resp);
    }

    static String getFileUrl(String service, String logfile)
    {
        return getFileUrl(service, logfile, false);
    }
    
    static String getFileUrl(String service, String logfile, boolean raw)
    {
        return "/logs?"+SERVICE+"="+service+"&"+LOGFILE+"="+logfile+(raw ? "&"+TYPE+"="+TYPE_RAW : "");
    }
    
    static String getFilesOfGroup(String service, String group)
    {
        return "/logs?"+SERVICE+"="+service+"&"+GROUP+"="+group;
    }
    
    private void showLogFileIndex(Services services, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Logfiles", req.getRequestURI(), out);
            
            out.append("<h1>Logfiles</h1>");
            
            for(Service service: services.getServices())
            {
                for(String logFile: service.getLogFiles().keySet())
                {
                    out.append("<a href=\"").append(ListServicesServlet.getUrl(service.getName())).append("\">").append(service.getName()).append("</a>");
                    out.append(" / <a href=\"").append(getFileUrl(service.getName(), logFile)).append("\">").append(LognameGenerator.getLogFileTitle(logFile)).append("</a><br>");
                }
            }
            
            Layout.end(out);
        }
    }
    
    private void showLogFileIndex(Service service, HttpServletRequest req, HttpServletResponse resp, String group) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        String prefix = LognameGenerator.getPrefix(group);
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Logfiles"+(prefix.isEmpty()?"":" "+prefix)+" of "+service.getName(), req.getRequestURI(), out);
            
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
            {
                if(!name.startsWith(prefix))
                    out.append("<a href=\""+getFileUrl(service.getName(), name)+"\">"+LognameGenerator.getLogFileTitle(name)+"</a><br>");
            }
            
            /*out.append("</body>");
            out.append("</head>");
            out.append("</html>");*/
            Layout.end(out);
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
            Layout.start(LognameGenerator.getLogFileTitle(logFile), req.getRequestURI(), out);
            
            out.append("<h1>"+LognameGenerator.getLogFileTitle(logFile)+"</h1>");
            
            out.append("<a href=\"").append(getFileUrl(service.getName(), logFile, true)).append("\">").append("Raw").append("</a>");
            
            out.append("<pre style=\"border: 1px solid #ccc;padding:0.5em\">");
            
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            Files.copy(path, arr);
            out.append(arr.toString().replace("<", "&lt;").replace(">", "&gt;"));
            
            out.append("</pre>");
            
            Layout.end(out);
        }
    }
}
