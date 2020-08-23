package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
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
            }).collect(Collectors.groupingBy(Function.identity(), () -> new EnumMap<>(StatusType.class), Collectors.counting()));
            
            out.append("<p>Service status:</p>");
            
            out.append("<div class=\"progress\">");
            counts.forEach((status, count) -> {
                double percentage = count*100./serviceList.size();
                
                out.append("<div class=\"progress-bar ");
                
                switch(status)
                {
                    case ACTIVE: out.append("bg-success"); break;
                    case INACTIVE: out.append("bg-secondary"); break;
                    case FAILED: out.append("bg-danger"); break;
                    default: out.append("bg-secondary");
                }
                
                out.append("\" role=\"progressbar\" style=\"width: ");
                out.append(percentage+"");
                out.append("%\" aria-valuenow=\""+count+"\" aria-valuemin=\"0\" aria-valuemax=\""+serviceList.size()+"\">");
                
                out.append(status.toString());
                out.append("</div>");
            });
            out.append("</div>");
            
            out.append("<p>Currently running tasks: ").append(pm.getProcesses().size()+"").append("</p>");
            
            Layout.end(out);
        }
    }
}
