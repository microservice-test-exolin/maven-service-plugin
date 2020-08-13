package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.servlet.Layout;
import static org.exolin.msp.web.ui.servlet.service.ListServicesServlet.write;

/**
 *
 * @author tomgk
 */
public class DeployServlet extends HttpServlet
{
    private final Services services;

    public DeployServlet(Services services)
    {
        this.services = services;
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
            write(out, "compile", "Compile");
            write(out, "deploy", "Deploy");
            out.append("</form>");
            
            Layout.end(out);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter service");
            return;
        }
        
        String action = req.getParameter("action");
        if(action == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter action");
            return;
        }
        
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
        
        switch(action)
        {
            case "compile":
            {
                try{
                    service.build(true);
                }catch(IOException|InterruptedException|UnsupportedOperationException e){
                    e.printStackTrace(resp.getWriter());
                    return;
                }
                
                break;
            }
            case "deploy":
            {
                try{
                    service.deploy(true);
                }catch(IOException|InterruptedException|UnsupportedOperationException e){
                    e.printStackTrace(resp.getWriter());
                    return;
                }
                
                break;
            }
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action "+action);
                return;
        }
        
        String referrer = req.getHeader("referer");
        if(referrer != null)
            resp.sendRedirect(referrer);
        else
            resp.sendRedirect(req.getRequestURI());  //back to GET
    }
}
