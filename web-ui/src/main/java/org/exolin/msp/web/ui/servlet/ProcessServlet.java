package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.web.ui.ProcessManager;
import org.exolin.msp.web.ui.Service;
import static org.exolin.msp.web.ui.servlet.ListServicesServlet.write;

/**
 *
 * @author tomgk
 */
public class ProcessServlet extends HttpServlet
{
    private final ProcessManager pm;

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
            out.append("<html>");
            out.append("<head>");
            out.append("<title>Processes</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");
            
            out.append("<h1>Processes</h1>");
            
            
            out.append("<table class=\"table\">");
            
            out.append("<tr>");
            out.append("<th>Name</th>");
            out.append("<th>Commandline</th>");
            out.append("<th>Runtime</th>");
            out.append("</tr>");
            
            List<ProcessManager.ProcessInfo> processes = pm.getProcesses();
            
            if(processes.isEmpty())
                out.append("<tr><td style=\"text-align: center\" colspan=\"3\"><em>No running processes</em></td></tr>");
            
            for(ProcessManager.ProcessInfo service: processes)
            {
                out.append("<tr>");
                out.append("<td>").append(service.getTitle()).append("</td>");
                out.append("<td>").append(String.join(" ", service.getCmd())).append("</td>");
                out.append("<td>").append(service.getRuntime()+" s").append("</td>");
                out.append("</tr>");
            }
            
            out.append("</table>");
            
            out.append("</div>");
            
            out.append("</body>");
            out.append("</html>");
        }
    }
}
