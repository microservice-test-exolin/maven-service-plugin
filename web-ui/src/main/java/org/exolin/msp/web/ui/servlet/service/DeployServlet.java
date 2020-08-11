package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.linux.LinuxService;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.Layout;
import static org.exolin.msp.web.ui.servlet.service.ListServicesServlet.write;

/**
 *
 * @author tomgk
 */
public class DeployServlet extends HttpServlet
{
    private final Services services;
    private final ProcessManager pm;

    public DeployServlet(Services services, ProcessManager pm)
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
            /*out.append("<html>");
            out.append("<head>");
            out.append("<title>Services</title>");
            out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
            out.append("<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>");
            out.append("</head>");
            
            out.append("<body>");
            
            out.append("<div class=\"container\">");*/
            Layout.start("Build/Deploy", req.getRequestURI(), out);
            
            out.append("<h1>Services</h1>");
            out.append("<form action=\"#\" method=\"POST\">");
            out.append("<input type=\"hidden\" name=\"service\" value=\"").append(service.getName()).append("\">");
            write(out, "compile", "Compile");
            write(out, "deploy", "Deploy");
            out.append("</form>");
            /*out.append("</div>");
            
            out.append("</body>");
            out.append("</html>");*/
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
                    service.build(pm);
                }catch(IOException|InterruptedException|UnsupportedOperationException e){
                    e.printStackTrace(resp.getWriter());
                    return;
                }
                
                resp.sendRedirect(LogServlet.getUrl(service.getName(), LinuxService.BUILD_LOG));
                
                return;
            }
            case "deploy":
            {
                try{
                    service.deploy(pm);
                }catch(IOException|InterruptedException|UnsupportedOperationException e){
                    e.printStackTrace(resp.getWriter());
                    return;
                }
                
                resp.sendRedirect(LogServlet.getUrl(service.getName(), LinuxService.DEPLOY_LOG));
                
                return;
            }
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action "+action);
                return;
        }
        
        //resp.sendRedirect(req.getRequestURI());  //back to GET
    }
}
