package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.service.ServiceServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class IndexServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexServlet.class);
    
    private final Services services;
    private final ProcessManager pm;

    public IndexServlet(Services services, ProcessManager pm)
    {
        this.services = services;
        this.pm = pm;
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if(req.getPathInfo() != null && !req.getPathInfo().equals("/"))
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        super.service(req, resp);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Service Web UI", req.getRequestURI(), out);
            
            out.append("<h1>Service Web UI</h1>");
            
            List<Service> serviceList = services.getServices();
            Map<StatusType, Long> counts = serviceList.stream().map(s -> {
                try{
                    return s.getStatus().getStatus();
                }catch(IOException|UnsupportedOperationException e){
                    LOGGER.error("Couldn't determine status", e);
                    return null;
                }
            }).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            
            out.append("<p>Service status:</p>");
            
            out.append("<div>");
            counts.forEach((status, count) -> {
                double percentage = count*100./serviceList.size();
                
                out.append("<div style=\"width: ");
                out.append(percentage+"");
                out.append("%;display:inline-block;border: 1px solid black;text-align:center;padding:0.25em;background:");
                
                switch(status)
                {
                    case ACTIVE: out.append("green"); break;
                    case INACTIVE: out.append("#ddd"); break;
                    case FAILED: out.append("red"); break;
                    default: out.append("white");
                }
                
                out.append("\">");
                out.append(status.toString());
                out.append("</div>");
            });
            out.append("</div>");
            
            out.append("<p>Currently running tasks: ").append(pm.getProcesses().size()+"").append("</p>");
            
            Layout.end(out);
        }
    }
}
