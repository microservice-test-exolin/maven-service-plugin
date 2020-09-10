package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.BuildOrDeployAlreadyRunningException;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.auth.AuthFilter;
import static org.exolin.msp.web.ui.servlet.service.ListServicesServlet.write;
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
                out.append("<form action=\"#\" method=\"POST\">");
                out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
                write(out, "compile", Icon.COMPILE, "Compile");
                write(out, "deploy", Icon.DEPLOY, "Deploy");
                out.append("</form>");

                Layout.end(out);
            }
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
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

            String user = (String)req.getAttribute(AuthFilter.USER);
            String initiator = "service-web-ui";
            if(user != null)
                initiator += "[user="+user+"]";

            switch(action)
            {
                case "compile":
                {
                    try{
                        service.build(true, initiator);
                    }catch(BuildOrDeployAlreadyRunningException e){
                        throw new HttpUtils.BadRequestMessage("Build or deploy already running");
                    }catch(IOException|InterruptedException|RuntimeException e){
                        LOGGER.error("Error while deploying", e);
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

                    break;
                }
                case "deploy":
                {
                    try{
                        service.deploy(true, initiator);
                    }catch(BuildOrDeployAlreadyRunningException e){
                        throw new HttpUtils.BadRequestMessage("Build or deploy already running");
                    }catch(IOException|InterruptedException|RuntimeException e){
                        LOGGER.error("Error while deploying", e);
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

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
