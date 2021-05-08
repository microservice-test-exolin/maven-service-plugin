package org.exolin.msp.web.ui.servlet.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.LognameGenerator;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
abstract class LogFileShowServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileShowServlet.class);
    
    static final String SERVICE = "service";
    static final String TASK = "task";
    static final String LOGFILE = "logfile";
    private static final String TYPE = "type";
    private static final String TYPE_RAW = "raw";
    
    static boolean isRawRequest(HttpServletRequest req)
    {
        return TYPE_RAW.equals(req.getParameter(TYPE));
    }
    
    static String getFileUrl(String service, Optional<String> taskName, String logfile, boolean raw)
    {
        StringBuilder path = new StringBuilder();
        
        if(taskName.isPresent())
            path.append(TaskLogFileShowServlet.URL);
        else
            path.append(ServiceLogFileShowServlet.URL);
        
        path.append("?" + SERVICE + "=").append(service);
        
        if(taskName.isPresent())
            path.append("&" + TASK + "=").append(taskName.get());
        
        path.append("&" + LOGFILE + "=").append(logfile);
        
        if(raw)
            path.append("&"+TYPE+"="+TYPE_RAW);
        
        return path.toString();
    }
    
    void showLogFile(Service service, Optional<String> taskName, String logFile, LogFile lf, boolean raw, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        if(raw)
            showLogFileRaw(logFile, lf, req, resp);
        else
            showLogFileWeb(service, taskName, logFile, lf, req, resp);
    }
    
    private void showLogFileRaw(String logFile, LogFile lf, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/plain;charset=UTF-8");
        if(lf == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Logfile "+logFile+" not found");
            return;
        }
        
        lf.writeTo(resp.getOutputStream());
    }
    
    private void showLogFileWeb(Service service, Optional<String> taskName, String logFile, LogFile lf, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
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
            
            out.append("<a href=\"").append(getFileUrl(service.getName(), taskName, logFile, true)).append("\">");
            Icon.CODE.writeTo(out);
            out.append("Raw").append("</a>");
            
            out.append("<pre class=\"logfile\">");
            
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            lf.writeTo(arr);
            
            List<String> logfileContent = Arrays.asList(arr.toString(StandardCharsets.UTF_8.name()).split("\r\n|\r|\n"));
            
            logfileContent.replaceAll(this::formatLogLine);
            
            logfileContent.forEach(out::println);
            
            out.append("</pre>");
            
            Layout.end(out);
        }
    }
    
    private static final String INFO = "[INFO] ";
    private static final String WARNING = "[WARNING] ";
    private static final String ERROR = "[ERROR] ";
    
    private static final String CLASS_INFO = "info";
    private static final String CLASS_WARNING = "warning";
    private static final String CLASS_ERROR = "error";
    private static final String CLASS_SUCCESS = "success";
    private static final String CLASS_GRAY = "gray";
    
    private String formatLogLine(String line)
    {
        line = line.replace("<", "&lt;").replace(">", "&gt;");
        String cssClass;
        
        //----------------------------------------------------------------------
        // Maven
        //----------------------------------------------------------------------
        if(line.equals("[INFO] BUILD SUCCESS"))
        {
            cssClass = CLASS_SUCCESS;
            line = "BUILD SUCCESS";
        }
        else if(line.startsWith(INFO+"--- ") && line.endsWith(" ---"))
        {
            cssClass = CLASS_GRAY;
            line = line.substring(INFO.length());
        }
        else if(line.startsWith(INFO))
        {
            cssClass = CLASS_INFO;
            line = line.substring(INFO.length());
        }
        else if(line.startsWith(WARNING))
        {
            cssClass = CLASS_WARNING;
            line = line.substring(WARNING.length());
        }
        else if(line.startsWith(ERROR))
        {
            cssClass = CLASS_ERROR;
            line = line.substring(ERROR.length());
        }
        //----------------------------------------------------------------------
        // Logback
        //----------------------------------------------------------------------
        else if(line.contains(" ERROR "))
            cssClass = CLASS_ERROR;
        else if(line.contains(" WARN  "))
            cssClass = CLASS_WARNING;
        else if(line.contains(" INFO  "))
            cssClass = CLASS_INFO;
        else
            cssClass = null;
        
        if(cssClass != null)
            line = "<span class=\""+cssClass+"\">"+line+"</span>";
        
        return line;
    }
}
