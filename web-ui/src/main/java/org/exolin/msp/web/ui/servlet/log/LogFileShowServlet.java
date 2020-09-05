package org.exolin.msp.web.ui.servlet.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
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

/**
 *
 * @author tomgk
 */
public class LogFileShowServlet extends HttpServlet
{
    public static final String URL = "/logfile";
    
    private static final String SERVICE = "service";
    private static final String LOGFILE = "logfile";
    private static final String TYPE = "type";
    private static final String TYPE_RAW = "raw";
    
    private final Services services;

    public LogFileShowServlet(Services services)
    {
        this.services = services;
    }

    public static String getFileUrl(String service, String logfile)
    {
        return getFileUrl(service, logfile, false);
    }
    
    static String getFileUrl(String service, String logfile, boolean raw)
    {
        return URL+"?"+SERVICE+"="+service+"&"+LOGFILE+"="+logfile+(raw ? "&"+TYPE+"="+TYPE_RAW : "");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter(SERVICE);
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing "+SERVICE+" parameter");
            return;
        }

        Service service = services.getService(serviceName);
        if(service == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
            return;
        }

        String logFile = req.getParameter(LOGFILE);
        if(TYPE_RAW.equals(req.getParameter(TYPE)))
            showLogFileRaw(service, logFile, req, resp);
        else
            showLogFile(service, logFile, req, resp);
    }
    
    private void showLogFileRaw(Service service, String logFile, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/plain;charset=UTF-8");
        LogFile lf = service.getLogFiles(null).get(logFile);
        if(lf == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Logfile "+logFile+" not found");
            return;
        }
        
        lf.writeTo(resp.getOutputStream());
    }
    
    private void showLogFile(Service service, String logFile, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        LogFile lf = service.getLogFiles(null).get(logFile);
        if(lf == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Logfile "+logFile+" not found");
            return;
        }
        
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start(LognameGenerator.getLogFileTitle(service.getName(), logFile), req.getRequestURI(), out);
            
            out.append("<h1>"+LognameGenerator.getLogFileTitle(service.getName(), logFile)+"</h1>");
            
            out.append("<a href=\"").append(getFileUrl(service.getName(), logFile, true)).append("\">");
            Icon.CODE.writeTo(out);
            out.append("Raw").append("</a>");
            
            out.append("<pre style=\"border: 1px solid #ccc;padding:0.5em\">");
            
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            lf.writeTo(arr);
            out.append(arr.toString().replace("<", "&lt;").replace(">", "&gt;"));
            
            out.append("</pre>");
            
            Layout.end(out);
        }
    }
}
