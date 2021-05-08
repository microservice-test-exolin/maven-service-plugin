package org.exolin.msp.web.ui.servlet.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.linux.LinuxService;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.log.TaskLogFileShowServlet;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessServlet.class);

    private static String format(LocalDateTime dateTime)
    {
        return dateTime.toString().replace('T', ' ');
    }

    private static void writeExitCode(PrintWriter out, Integer exitCode)
    {
        if(exitCode == null)
            out.append("<span class=\"badge badge-secondary\">unknown</span>");
        else if(exitCode == 0)
            out.append("<span class=\"badge badge-success\">successful</span>");
        else
            out.append("<span class=\"badge badge-danger\">failed (").append(exitCode+"").append(")</span>");
    }
    
    private final ProcessManager pm;

    public static final String URL = "/processes/running";

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
            
            list(out, pm.getProcesses(), true, true, "No running processes");
            
            Layout.end(out);
        }
    }
    
    public static void list(PrintWriter out, List<ProcessInfo> processes, boolean showServiceTitle, boolean details, String emptyText)
    {
        out.append("<style>.initiator{white-space: nowrap}.cmd, .pwd{font-family: Consolas, Monospace}</style>");
        
        out.append("<div class=\"table-responsive\">");
        out.append("<table class=\"table table-striped table-sm\">");

        out.append("<tr>");
        
        if(showServiceTitle)
            out.append("<th>Service</th>");
        
        out.append("<th>Name</th>");
        out.append("<th>Initiater</th>");
        
        if(details)
        {
            out.append("<th>Commandline</th>");
            out.append("<th>Working directory</th>");
        }
        
        out.append("<th>Started at</th>");
        out.append("<th style=\"text-align: right\">Runtime</th>");
        out.append("<th>Log</th>");
        out.append("<th>Result</th>");
        out.append("</tr>");

        if(processes.isEmpty() && emptyText != null)
            out.append("<tr><td style=\"text-align: center\" colspan=\"9\"><em>").append(emptyText).append("</em></td></tr>");

        for(ProcessInfo process: processes)
        {
            long runtime = process.getRuntime();
            
            String logFileName = LinuxService.getLogicalLogFileName(process.getName(), process.getStartTime());
            
            out.append("<tr>");
            
            if(showServiceTitle)
                out.append("<td><a href=\"").append(ListServicesServlet.getUrl(process.getService())).append("\">").append(process.getService()).append("</a></td>");
            
            out.append("<td>").append(process.getName()).append("</td>");
            out.append("<td class=\"initiator\">").append(Optional.ofNullable(process.getInitiator()).map(InitiatorFormatter::displayInitiator).orElse("<em>unknown</em>")).append("</td>");
            
            if(details)
            {
                out.append("<td class=\"cmd\">").append(process.getCmd()).append("</td>");
                out.append("<td class=\"pwd\">").append(Optional.ofNullable(process.getWorkingDirectory()).map(Path::toString).orElse("<em>unknown</em>")).append("</td>");
            }
            
            out.append("<td>").append(format(process.getStartedAt())).append("</td>");
            out.append("<td style=\"text-align: right\">").append(runtime != -1 ? runtime/1000.+" s" : "<em>N/A</em>").append("</td>");
            out.append("<td><a href=\"").append(TaskLogFileShowServlet.getFileUrl(process.getService(), process.getName(), logFileName)).append("\">Logfile</a></td>");
            out.append("<td>");
            writeExitCode(out, process.getExitCode());
            out.append("</td>");
            out.append("</tr>");
        }

        out.append("</table>");
        out.append("</div>");
    }
}
