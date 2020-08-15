package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.pm.ProcessInfo;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;

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
            /*out.append("<html>");
            out.append("<head>");
            out.append("<title>Processes</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");*/
            Layout.start("Processes", req.getRequestURI(), out);
            
            out.append("<h1>Processes</h1>");
            
            list(out, pm.getProcesses());
            List<ProcessInfo> processesHistory = pm.getProcessesHistory();
            if(!processesHistory.isEmpty())
            {
                out.append("<h2>History</h2>");
                list(out, processesHistory);
            }
            
            /*out.append("</div>");
            
            out.append("</body>");
            out.append("</html>");*/
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
        out.append("<th>Runtime</th>");
        out.append("</tr>");

        if(processes.isEmpty())
            out.append("<tr><td style=\"text-align: center\" colspan=\"4\"><em>No running processes</em></td></tr>");

        for(ProcessInfo process: processes)
        {
            long runtime = process.getRuntime();
            
            out.append("<tr>");
            out.append("<td><a href=\"").append(ListServicesServlet.getUrl(process.getService())).append("\">").append(process.getService()).append("</a></td>");
            out.append("<td>").append(process.getTitle()).append("</td>");
            out.append("<td>").append(String.join(" ", process.getCmd())).append("</td>");
            out.append("<td>").append(runtime != -1 ? runtime+" s" : "<em>N/A</em>").append("</td>");
            out.append("</tr>");
        }

        out.append("</table>");
    }
}
