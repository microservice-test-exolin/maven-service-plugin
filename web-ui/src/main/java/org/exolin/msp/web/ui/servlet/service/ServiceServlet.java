package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.HtmlUtils;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.ReverseList;
import org.exolin.msp.web.ui.servlet.task.ProcessServlet;
import org.exolin.msp.web.ui.servlet.task.TaskLogServlet;
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
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            String serviceName = HttpUtils.getRequiredParameter(req, "service");
            
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
            writeServiceCard(service, out);
            writeBuildDeployCard(service, out);
            writeGitCard(service.getGitRepository().get(), out);
            out.append("<div style=\"clear:both\"></div>");
            out.append("</div>");
            
            out.append("<h2>Builds/deploys</h2>");
            ProcessServlet.list(out, new ReverseList<>(pm.getProcessesIncludingHistory(service.getName())), false, false, "No processes run yet");
            
            Layout.end(out);
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }catch(IOException|RuntimeException e){
            LOGGER.error("Failed", e);
            throw e;
        }
    }

    public static void writeStatus(PrintWriter out, StatusType statusType, StatusInfo.UnknowableBoolean enabled)
    {
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
        
        switch(enabled)
        {
            case TRUE:
                out.append(" <span title=\"running\" class=\"badge badge-success\">enabled</span>");
                break;

            case FALSE:
                out.append(" <span title=\"failed to start\" class=\"badge badge-danger\">disabled</span>");
                break;
                
            case UNKNOWN: break;
            
            default:
                out.append(" <span class=\"badge badge-secondary\">enabled="+enabled+"</span>");
        }
    }
    
    static void writeStatus(PrintWriter out, StatusInfo status)
    {
        writeStatus(out, status.getStatus(), status.isStartAtBootEnabled());
    }

    private void writeServiceCard(Service service, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");// style=\"max-width: 25rem;\">");
        out.append("<div class=\"card-header\">Service</div>\n");

        out.append("<table class=\"table table-sm\">");

        out.append("<tr>");
        out.append("<th>Name</th>");
        out.append("<td>").append(service.getName()).append("</td>");
        out.append("</tr>");
        
        StatusInfo status = null;
        try{
            status = service.getApplicationInstance().getStatus();
        }catch(IOException e){
            LOGGER.error("Couldn't be determined", e);
            out.append("Couldn't be determined");
        }

        out.append("<tr>");
        out.append("<th>Status</th>");
        out.append("<td>");
        if(status != null)
            writeStatus(out, status);
        out.append("</td>");
        out.append("</tr>");

        out.append("<tr>");
        out.append("<th>Memory</th>");
        out.append("<td>");
        if(status != null)
        {
            String memory = status.getMemory();
            if(memory != null)
                out.append(memory);
            else
                out.append("<em>unknown</em>");
        }
        out.append("</td>");
        out.append("</tr>");

        out.append("<tr>");
        out.append("<th>Java Options:</th>");
        out.append("<td>");
        if(status != null)
        {
            String javaOptions = status.getJavaOptions();
            if(javaOptions != null)
            {
                if(!javaOptions.isEmpty())
                    out.append(javaOptions);
                else
                    out.append("<em>none</em>");
            }
            else
                out.append("<em>unknown</em>");
        }
        out.append("</td>");
        out.append("</tr>");

        out.append("<tr>");
        out.append("<th>Links</th>");
        out.append("<td>");
        out.append("<a href=\""+ServiceStatusServlet.getUrl(service.getName())+"\">");
        Icon.INFO.writeTo(out);
        out.append("Status</a>");
        out.append("<br>");
        out.append("<a href=\""+ServiceLogServlet.getFilesOfService(service.getName())+"\">");
        Icon.LOG.writeTo(out);
        out.append("Service Logfiles</a><br>");
        out.append("</td>");
        out.append("</tr>");
        out.append("</table>");

        out.append("<div class=\"card-body\">");
        HtmlUtils.startInlineForm(out, ListServicesServlet.URL);
        HtmlUtils.writeHiddenInput(out, "service", service.getName());
        HtmlUtils.writeActionButton(out, "start", Icon.START, "Start");
        HtmlUtils.writeActionButton(out, "stop", Icon.STOP, "Stop");
        HtmlUtils.writeActionButton(out, "restart", Icon.RESTART, "Restart");
        HtmlUtils.endInlineForm(out);

        out.append("</div></div>");
    }

    private void writeBuildDeployCard(Service service, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");// style=\"max-width: 25rem;\">");
        out.append("<div class=\"card-header\">Build/Deployment</div>\n");
        out.append("<div class=\"card-body\">");
        out.append("<a href=\"").append(TaskLogServlet.getFilesOfTask(service.getName(), "build")).append("\">");
        Icon.LOG.writeTo(out);
        out.append("Build Logfiles</a><br>");
        out.append("<a href=\""+TaskLogServlet.getFilesOfTask(service.getName(), "deploy")+"\">");
        Icon.LOG.writeTo(out);
        out.append("Deploy Logfiles</a><br>");
        DeployServlet.writeButtons(service, service.getGitRepository(), out);
        /*if(gitRepository.isPresent() && DeployServlet.supportAnyButton(gitRepository.get()))
        {
            if(!gitRepository.get().isTaskRunning())
            {
                out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "compile", Icon.COMPILE, "Compile");
                write(out, "deploy", Icon.DEPLOY, "Deploy");
                out.append("</form>");
            }
            else
                out.append("Build/deploy currently running");
        }*/
        out.append("</div></div>");
    }

    private void writeGitCard(GitRepository repo, PrintWriter out) throws IOException
    {
        out.append("<div class=\"card\">");
        out.append("<div class=\"card-header\">Git</div>\n");
        out.append("<div class=\"card-body\">");
        
        String repoUrl = repo.getRepositoryUrl();
        String host = new URL(repoUrl).getHost();
        String name = host;
        
        Path localMavenProject = repo.getLocalServiceMavenProject();
        Path localGitRepo = repo.getLocalGitRoot();
        
        out.append("<p>");
        out.append("<a href=\"").append(repoUrl).append("\"");
        
        if(host.equals("github.com"))
        {
            out.append(" style=\"background-image: url('/GitHub_Logo_small.png'); background-repeat: no-repeat; background-position: left; padding-left: 36px\"");
            name = new URL(repoUrl).getPath().substring(1);
        }
        
        out.append(">");
        out.append(name).append("</a>");
        
        if(!localGitRepo.equals(localMavenProject))
        {
            String rel = localGitRepo.relativize(localMavenProject).toString();
            
            out.append(" / ");
            out.append("<a title=\"subdirectory in the repository which contains the service project\" ");
            out.append("href=\"").append(repoUrl).append("/tree/master/").append(rel).append("\">");
            out.append(rel);
            out.append("</a>");
        }
        
        out.append("</p>");
        
        out.append("<p style=\"color: gray\">Local Git Repository: ").append(localGitRepo.toString()).append("</p>");
        
        /*out.append("<table>");// class=\"table\">");
        out.append("<tr><th>Repository URL:</th><td>").append(service.getRepositoryUrl()).append("</td></tr>");
        out.append("<tr><th>Local Git Repo:</th><td>").append(service.getLocalGitRoot().toString()).append("</td></tr>");
        out.append("</table>");*/
        out.append("</div></div>");
    }
}
