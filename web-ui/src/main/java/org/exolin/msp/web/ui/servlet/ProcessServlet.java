package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.LogFileShowServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessServlet.class);
    
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
                list(out, new ReverseList<>(processesHistory));
            }
            
            Layout.end(out);
        }
    }
    
    {
        out.append("<table class=\"table table-striped table-sm\">");

        out.append("<tr>");
        out.append("<th>Service</th>");
        out.append("<th>Name</th>");
        out.append("<th>Title</th>");
        out.append("<th>Initiater</th>");
        out.append("<th>Commandline</th>");
        out.append("<th>Working directory</th>");
        out.append("<th>Started at</th>");
        out.append("<th>Runtime</th>");
        out.append("<th>Log</th>");
        out.append("<th>Exit Code</th>");
        out.append("</tr>");

        if(processes.isEmpty())
            out.append("<tr><td style=\"text-align: center\" colspan=\"9\"><em>No running processes</em></td></tr>");

        for(ProcessInfo process: processes)
        {
            long runtime = process.getRuntime();
            
            //TODO: passt so nicht
            String logFileName = process.getName()+" "+process.getStartTime()+".log";
            
            out.append("<tr>");
            out.append("<td><a href=\"").append(ListServicesServlet.getUrl(process.getService())).append("\">").append(process.getService()).append("</a></td>");
            out.append("<td>").append(process.getName()).append("</td>");
            out.append("<td>").append(process.getTitle()).append("</td>");
            out.append("<td>").append(Optional.ofNullable(process.getInitiator()).map(ProcessServlet::displayInitiator).orElse("<em>unknown</em>")).append("</td>");
            out.append("<td>").append(String.join(" ", process.getCmd())).append("</td>");
            out.append("<td>").append(Optional.ofNullable(process.getWorkingDirectory()).map(Path::toString).orElse("<em>unknown</em>")).append("</td>");
            out.append("<td>").append(process.getStartedAt().toString()).append("</td>");
            out.append("<td>").append(runtime != -1 ? runtime+" s" : "<em>N/A</em>").append("</td>");
            out.append("<td><a href=\"").append(LogFileShowServlet.getFileUrl(process.getService(), logFileName)).append("\">Logfile</a></td>");
            out.append("<td>").append(Optional.ofNullable(process.getExitCode()).map(Object::toString).orElse("<em>unknown</em>")).append("</td>");
            out.append("</tr>");
        }

        out.append("</table>");
    }
    
    static class Initiator
    {
        private final String type;
        private final Map<String, String> args;

        public Initiator(String type, Map<String, String> args)
        {
            this.type = type;
            this.args = args;
        }
        
        public static Initiator parse(String initiator)
        {
            String type;
            Map<String, String> map = new HashMap<>();
            
            if(!initiator.contains("["))
                type = initiator;
            else if(!initiator.endsWith("]"))
                throw new IllegalArgumentException(initiator);
            else
            {
                int i = initiator.indexOf('[');
                type = initiator.substring(0, i);
                String mapString = initiator.substring(i+1, initiator.length()-1);
                String[] pairs = mapString.split(",");
                for(String pair: pairs)
                {
                    int eq = pair.indexOf('=');
                    if(eq == -1)
                        throw new IllegalArgumentException(initiator);
                    map.put(pair.substring(0, eq), pair.substring(eq+1));
                }
            }
            
            return new Initiator(type, map);
        }
    }
    
    static String displayInitiator(String initiator)
    {
        Initiator i;
        try{
            i = Initiator.parse(initiator);
        }catch(IllegalArgumentException e){
            LOGGER.error("Couldn't parse", e);
            return initiator;
        }
        
        if(i.type.equals("github-webhook"))
        {
            String repo = i.args.get("repo");
            String sha1 = i.args.get("sha1");
            if(repo != null && sha1 != null)
                return "<a href=\""+repo+"/commit/"+sha1+"\">Github Webhook</a>";
            else if(repo != null && sha1 == null)
                return "<a href=\""+repo+"\">Github Webhook</a>";
            else
                return "Github Webhook";
        }
        if(i.type.equals("service-web-ui"))
        {
            String user = i.args.get("user");
            if(user != null)
                return "Service Web UI by "+user;
            else
                return "Service Web UI";
        }
        else
            return initiator;
    }
}
