package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusInfo;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.ApplicationInstance;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.HtmlUtils;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.task.TaskLogServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ListServicesServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ListServicesServlet.class);
    
    private final Services services;
    
    public static final String URL = "/services";

    public ListServicesServlet(Services services)
    {
        this.services = services;
    }

    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_RESTART = "restart";
    private static final String ACTION_ENABLE = "enable";
    private static final String ACTION_DISABLE = "disable";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        boolean showBuildOptions = "1".equals(req.getParameter("showBuildOptions"));
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Services", req.getRequestURI(), out);
            //out.append("<div class=\"d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom\">");
            
            out.append("<h1>Services</h1>");
            
            out.append("<table class=\"table table-striped table-sm\">");
            
            out.append("<tr>");
            out.append("<th>Name</th>");
            out.append("<th>Status</th>");
            out.append("<th>Memory</th>");
            if(showBuildOptions)
                out.append("<th colspan=\"3\"></th>");
            else
                out.append("<th></th>");
            out.append("</tr>");
            
            for(Service service: services.getServices())
            {
                out.append("<tr id=\"").append(service.getName()).append("\">");
                out.append("<td><a href=\"").append(getUrl(service.getName())).append("\">").append(service.getName()).append("</a></td>");
                
                StatusInfo status;
                try{
                    status = service.getApplicationInstance().getStatus();
                }catch(IOException e){
                    status = null;
                    LOGGER.error("Couldn't be determined", e);
                }
                
                out.append("<td>");
                
                if(status != null)
                    ServiceServlet.writeStatus(out, status);
                else
                    out.append("Couldn't be determined");
                
                out.append("</td>");
                
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
                
                out.append("<td>");
                HtmlUtils.startInlineForm(out, "#");
                HtmlUtils.writeHiddenInput(out, "service", service.getName());
                
                if(status != null && status.getStatus() != StatusType.ACTIVE)
                    HtmlUtils.writeActionButton(out, ACTION_START, Icon.START, "Start");
                if(status != null && status.getStatus() != StatusType.INACTIVE)
                    HtmlUtils.writeActionButton(out, ACTION_STOP, Icon.STOP, "Stop");
                if(status != null && status.getStatus() != StatusType.INACTIVE)
                    HtmlUtils.writeActionButton(out, ACTION_RESTART, Icon.RESTART, "Restart");
                
                if(status != null && status.isStartAtBootEnabled() != StatusInfo.UnknowableBoolean.TRUE)
                    HtmlUtils.writeActionButton(out, ACTION_ENABLE, null, "Enable");
                if(status != null && status.isStartAtBootEnabled() != StatusInfo.UnknowableBoolean.FALSE)
                    HtmlUtils.writeActionButton(out, ACTION_DISABLE, null, "Disable");
                
                HtmlUtils.endInlineForm(out);
                
                out.append("<a href=\""+ServiceStatusServlet.getUrl(service.getName())+"\">Status</a> ");
                out.append("<a href=\""+ServiceLogServlet.getFilesOfService(service.getName())+"\">Logfiles</a><br>");
                
                out.append("</td>");
                
                if(showBuildOptions)
                {
                    out.append("<td>");
                    DeployServlet.writeButtons(service, service.getGitRepository(), out);
                    /*if(repository.isPresent())
                    {
                        if(DeployServlet.supportAnyButton(repo))
                        {
                            if(!repo.isTaskRunning())
                            {
                                out.append("<form action=\"/deploy\" method=\"POST\" style=\"display: inline\">");
                                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                                
                                if(repo.supports(GitRepository.Task.BUILD))
                                    write(out, DeployServlet.ACTION_BUILD, Icon.COMPILE, "Compile");
                                
                                if(repo.supports(GitRepository.Task.DEPLOY))
                                    write(out, DeployServlet.ACTION_DEPLOY, Icon.DEPLOY, "Deploy");
                                
                                if(repo.supports(GitRepository.Task.BUILD_AND_DEPLOY))
                                    write(out, DeployServlet.ACTION_BUILD_AND_DEPLOY, Icon.DEPLOY, "Build & Deploy");
                                
                                out.append("</form>");
                            }
                            else
                                out.append("Build/deploy currently running");
                        }
                    }*/
                    out.append("</td>");

                    out.append("<td>");
                    out.append("<a href=\""+TaskLogServlet.getFilesOfTask(service.getName(), "build")+"\">Build Logfiles</a><br>");
                    out.append("<a href=\""+TaskLogServlet.getFilesOfTask(service.getName(), "deploy")+"\">Deploy Logfiles</a><br>");

                    out.append("</td>");
                }
                
                out.append("</tr>");
            }
            
            out.append("</table>");
            
            //out.append("</div>");
            Layout.end(out);
        }
    }

    public static String getUrl(String service)
    {
        return "/service?service="+service;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            String serviceName = HttpUtils.getRequiredParameter(req, "service");
            String action = HttpUtils.getRequiredParameter(req, "action");

            Service service;
            try{
                service = services.getService(serviceName);
            }catch(IOException e){
                throw new ServletException(e);
            }
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
                return;
            }
            
            ApplicationInstance instance = service.getApplicationInstance();

            switch(action)
            {
                case ACTION_START: instance.start(); break;
                case ACTION_STOP: instance.stop(); break;
                case ACTION_RESTART: instance.restart(); break;
                case ACTION_ENABLE: instance.setStartAtBoot(true); break;
                case ACTION_DISABLE: instance.setStartAtBoot(false); break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
            }

            resp.sendRedirect(req.getRequestURI());  //back to GET
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
