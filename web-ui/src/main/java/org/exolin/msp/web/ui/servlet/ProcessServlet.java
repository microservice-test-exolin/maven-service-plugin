package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogFileShowServlet;

/**
 *
 * @author tomgk
 */
public class ProcessServlet extends HttpServlet
{
    private final ProcessManager pm;

    public static final String URL = "/processes";

    public ProcessServlet(ProcessManager pm)
    {
        this.pm = pm;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Processes", req.getRequestURI(), out);
            
            out.append("<h1>Processes</h1>");
            
            list(out, pm.getProcesses());
            List<ProcessInfo> processesHistory = pm.getProcessesHistory();
            if(!processesHistory.isEmpty())
            {
                out.append("<h2>History</h2>");
                list(out, processesHistory);
            }
            
            Layout.end(out);
        }
    }
    
    private void list(PrintWriter out, List<ProcessInfo> processes)
    {
        out.append("<table class=\"table table-striped table-sm\">");

        out.append("<tr>");
        out.append("<th>Service</th>");
        out.append("<th>Name</th>");
        out.append("<th>Commandline</th>");
        out.append("<th>Started at</th>");
        out.append("<th>Runtime</th>");
        out.append("<th>Log</th>");
        out.append("</tr>");

        if(processes.isEmpty())
            out.append("<tr><td style=\"text-align: center\" colspan=\"4\"><em>No running processes</em></td></tr>");

        for(ProcessInfo process: processes)
        {
            long runtime = process.getRuntime();
            
            //TODO: passt so nicht
            String logFileName = process.getName()+" "+process.getStartTime()+".log";
            
            out.append("<tr>");
            out.append("<td><a href=\"").append(ListServicesServlet.getUrl(process.getService())).append("\">").append(process.getService()).append("</a></td>");
            out.append("<td>").append(process.getTitle()).append("</td>");
            out.append("<td>").append(String.join(" ", process.getCmd())).append("</td>");
            out.append("<td>").append(process.getStartedAt().toString()).append("</td>");
            out.append("<td>").append(runtime != -1 ? runtime+" s" : "<em>N/A</em>").append("</td>");
            out.append("<td><a href=\"").append(LogFileShowServlet.getFileUrl(process.getService(), logFileName)).append("\">Logfile</a></td>");
            out.append("</tr>");
        }

        out.append("</table>");
    }
}
