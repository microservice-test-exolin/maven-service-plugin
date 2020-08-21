package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.ProcessServlet;
import org.exolin.msp.web.ui.servlet.ReverseList;
import static org.exolin.msp.web.ui.servlet.service.ListServicesServlet.write;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ServiceServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServlet.class);
    
    private final Services services;
    private final ProcessManager pm;
    
    public static final String URL = "/service";

    public ServiceServlet(Services services, ProcessManager pm)
    {
        this.services = services;
        this.pm = pm;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
            return;
        }
        
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Service service = services.getService(serviceName);
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service not found");
                return;
            }
            
            Layout.start("Service "+serviceName, req.getRequestURI(), out);
            
            out.append("<h1>Service "+serviceName+"</h1>");
            
            out.append("<style>.card-row .card{float: left; margin-right: 1em}</style>");
            out.append("<div class=\"card-row\">");
            writeCard1(service, out);
            writeCard2(service, out);
            writeCard3(service, out);
            out.append("<div style=\"clear:both\"></div>");
            out.append("</div>");
            
            out.append("<h2>Builds/deploys</h2>");
            ProcessServlet.list(out, new ReverseList<>(pm.getProcessesIncludingHistory(service.getName())), false, false);
            
            //out.append("</div>");
            Layout.end(out);
        }
    }

    static void writeStatus(PrintWriter out, StatusInfo status)
    {
        try{
            StatusType statusType = status.getStatus();
            
            switch(statusType)
            {
                case ACTIVE:
                    out.append("<span title=\"running\" class=\"badge badge-success\">running</span>");
                    break;
                    
                case FAILED:
                    out.append("<span title=\"failed to start\" class=\"badge badge-danger\">failed</span>");
                    break;
                    
                case INACTIVE:
                    out.append("<span title=\"not started\" class=\"badge badge-secondary\">inactive</span>");
                    break;
                    
                default:
                    out.append("<span class=\"badge badge-secondary\">"+statusType+"</span>");
                    break;
            }
        }catch(UnsupportedOperationException e){
            LOGGER.warn("Couldn't determine status", e);
            out.append("<span title=\"unknown because failed to fetch status\" class=\"badge badge-secondary\">unknown</span>");
        }
    }

    private void writeCard1(Service service, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");// style=\"max-width: 25rem;\">");
        out.append("<div class=\"card-header\">Service</div>\n");

        out.append("<table class=\"table table-sm\">");

        out.append("<tr>");
        out.append("<th>Name</th>");
        out.append("<td>").append(service.getName()).append("</td>");
        out.append("</tr>");

        out.append("<tr>");
        out.append("<th>Status</th>");
        out.append("<td>");
        try{
            writeStatus(out, service.getStatus());
        }catch(IOException e){
            LOGGER.error("Couldn't be determined", e);
            out.append("Couldn't be determined");
        }
        out.append("</td>");
        out.append("</tr>");

        out.append("<tr>");
        out.append("<th>Links</th>");
        out.append("<td>");
        out.append("<a href=\""+ServiceStatusServlet.getUrl(service.getName())+"\">");
        out.append("<span data-feather=\"info\"></span> ");
        out.append("Status</a>");
        out.append("<br>");
        out.append("<a href=\""+LogServlet.getFilesOfService(service.getName())+"\">");
        out.append("<span data-feather=\""+Layout.LOG+"\"></span> ");
        out.append("Service Logfiles</a><br>");
        out.append("</td>");
        out.append("</tr>");
        out.append("</table>");

        out.append("<div class=\"card-body\">");
        out.append("<form action=\""+ListServicesServlet.URL+"\" method=\"POST\" style=\"display: inline\">");
        out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
        write(out, "start", Layout.START, "Start");
        write(out, "stop", Layout.STOP, "Stop");
        write(out, "restart", Layout.RESTART, "Restart");
        out.append("</form>");

        out.append("</div></div>");
    }

    private void writeCard2(Service service, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");// style=\"max-width: 25rem;\">");
        out.append("<div class=\"card-header\">Build/Deployment</div>\n");
        out.append("<div class=\"card-body\">");
        out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "build")+"\">");
        out.append("<span data-feather=\""+Layout.LOG+"\"></span> ");
        out.append("Build Logfiles</a><br>");
        out.append("<a href=\""+LogServlet.getFilesOfTask(service.getName(), "deploy")+"\">");
        out.append("<span data-feather=\""+Layout.LOG+"\"></span> ");
        out.append("Deploy Logfiles</a><br>");
        if(service.supportsBuildAndDeployment())
        {
            out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
            if(!service.isBuildOrDeployProcessRunning())
            {
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "compile", Layout.COMPILE, "Compile");
                write(out, "deploy", Layout.DEPLOY, "Deploy");
                out.append("</form>");
            }
            else
                out.append("Build/deploy currently running");
        }
        out.append("</div></div>");
    }

    private void writeCard3(Service service, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");// style=\"max-width: 25rem;\">");
        out.append("<div class=\"card-header\">Git</div>\n");
        out.append("<div class=\"card-body\">");
        
        String repoUrl = service.getRepositoryUrl();
        String host = new URL(repoUrl).getHost();
        String name = host;
        
        out.append("<a href=\"").append(repoUrl).append("\"");
        
        if(host.equals("github.com"))
        {
            out.append(" style=\"background-image: url('/GitHub_Logo_small.png'); background-repeat: no-repeat; background-position: left; padding-left: 36px\"");
            name = new URL(repoUrl).getPath().substring(1);
        }
        
        out.append(">");
        out.append(name).append("</a>");
        
        /*out.append("<table>");// class=\"table\">");
        out.append("<tr><th>Repository URL:</th><td>").append(service.getRepositoryUrl()).append("</td></tr>");
        out.append("<tr><th>Local Git Repo:</th><td>").append(service.getLocalGitRoot().toString()).append("</td></tr>");
        out.append("</table>");*/
        out.append("</div></div>");
    }
}
