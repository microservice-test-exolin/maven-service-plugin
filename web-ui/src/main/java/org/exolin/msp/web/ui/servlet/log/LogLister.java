package org.exolin.msp.web.ui.servlet.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;

/**
 *
 * @author tomgk
 */
public class LogLister
{
    public static void listServiceFiles(Service service, Optional<String> taskName, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Logfiles"+taskName.map(t -> " "+t).orElse("")+" of "+service.getName(), req.getRequestURI(), out);
            
            out.append("<h1>Logfiles"+taskName.map(t -> " "+t).orElse("")+" of "+service.getName()+"</h1>");
            
            Map<String, LogFile> files = service.getLogFiles(taskName);
            files.forEach((name, lf) -> {
                out.append("<a href=\""+LogFileShowServlet.getFileUrl(service.getName(), name)+"\">");
                Icon.LOG.writeTo(out);
                out.append(lf.getTitle()).append("</a><br>");
            });
            if(files.isEmpty())
                out.append("<em>No files found</em>");
            
            Layout.end(out);
        }
    }
}
