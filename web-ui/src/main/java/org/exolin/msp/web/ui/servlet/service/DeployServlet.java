package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.TaskAlreadyRunningException;
import org.exolin.msp.web.ui.HtmlUtils;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.auth.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class DeployServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployServlet.class);

    public static final String URL = "/deploy";
    
    private final Services services;

    public DeployServlet(Services services)
    {
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            String serviceName = HttpUtils.getRequiredParameter(req, "service");

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

            resp.setContentType("text/html;charset=UTF-8");

            try(PrintWriter out = resp.getWriter())
            {
                Layout.start("Build/Deploy", req.getRequestURI(), out);

                out.append("<h1>Services</h1>");
                
                writeButtons(service, service.getGitRepository(), out);

                Layout.end(out);
            }
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    static boolean supportAnyButton(GitRepository repo) throws IOException
    {
        return repo.supports(GitRepository.Task.BUILD) ||
               repo.supports(GitRepository.Task.DEPLOY) ||
               repo.supports(GitRepository.Task.BUILD_AND_DEPLOY);
    }
    
    static void writeButtons(Service service, Optional<GitRepository> gitRepository, PrintWriter out) throws IOException
    {
        if(gitRepository.isPresent() && supportAnyButton(gitRepository.get()))
            writeButtons(service, gitRepository.get(), out);
        else
            out.append("Build/deploy not supported");
    }
    
    static void writeButtons(Service service, GitRepository gitRepository, PrintWriter out) throws IOException
    {
        if(!gitRepository.isTaskRunning())
        {
            HtmlUtils.startInlineForm(out, URL);
            HtmlUtils.writeHiddenInput(out, "service", service.getName());

            if(gitRepository.supports(GitRepository.Task.BUILD))
                HtmlUtils.writeActionButton(out, ACTION_BUILD, Icon.COMPILE, "Compile");

            if(gitRepository.supports(GitRepository.Task.DEPLOY))
                HtmlUtils.writeActionButton(out, ACTION_DEPLOY, Icon.DEPLOY, "Deploy");

            if(gitRepository.supports(GitRepository.Task.BUILD_AND_DEPLOY))
                HtmlUtils.writeActionButton(out, ACTION_BUILD_AND_DEPLOY, Icon.DEPLOY, "Build & Deploy");

            HtmlUtils.endInlineForm(out);
        } else {
            out.append("Build/deploy currently running");
        }
    }
    
    public static final String ACTION_BUILD = "compile";
    public static final String ACTION_DEPLOY = "deploy";
    public static final String ACTION_BUILD_AND_DEPLOY = "buildAndDeploy";
    
    private boolean runTask(Service service, GitRepository.Task task, String initiator, HttpServletResponse resp) throws HttpUtils.BadRequestMessage, IOException
    {
        try{
            Optional<GitRepository> gitRepository = service.getGitRepository();
            if(!gitRepository.isPresent())
                throw new HttpUtils.BadRequestMessage("Service has no repository");

            gitRepository.get().run(task, true, initiator);
            return true;
        }catch(TaskAlreadyRunningException e){
            throw new HttpUtils.BadRequestMessage("A task is already running for the service "+service.getName());
        }catch(IOException|InterruptedException|RuntimeException e){
            LOGGER.error("Error while deploying", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }
    
    public static final String INITIATOR_WEB_UI = "service-web-ui";
    public static final String NAME_USER = "user";

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

            String user = (String)req.getAttribute(AuthFilter.USER);
            String initiator = INITIATOR_WEB_UI;
            if(user != null)
                initiator += "["+NAME_USER+"="+user+"]";

            switch(action)
            {
                case "compile":
                {
                    if(!runTask(service, GitRepository.Task.BUILD, initiator, resp))
                        return;

                    break;
                }
                case "deploy":
                {
                    if(!runTask(service, GitRepository.Task.DEPLOY, initiator, resp))
                        return;

                    break;
                }
                case "buildAndDeploy":
                {
                    if(!runTask(service, GitRepository.Task.BUILD_AND_DEPLOY, initiator, resp))
                        return;

                    break;
                }
                default:
                    throw new HttpUtils.BadRequestMessage("Unknown action "+action);
            }

            String referrer = req.getHeader("referer");
            if(referrer != null)
                resp.sendRedirect(referrer);
            else
                resp.sendRedirect(req.getRequestURI());  //back to GET
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
