package org.exolin.msp.web.ui.servlet.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.pm.ProcessInfo;
import org.exolin.msp.service.pm.ProcessManager;
import org.exolin.msp.web.ui.servlet.Layout;
import org.exolin.msp.web.ui.servlet.ReverseList;
import static org.exolin.msp.web.ui.servlet.task.ProcessServlet.list;

/**
 *
 * @author tomgk
 */
public class ProcessHistoryServlet extends HttpServlet
{
    private final ProcessManager pm;

    public static final String URL = "/processes/history";

    public ProcessHistoryServlet(ProcessManager pm)
    {
        this.pm = pm;
    }

    private static Predicate<ProcessInfo> combine(Predicate<ProcessInfo> a, Predicate<ProcessInfo> b)
    {
        if(a == null)
            return b;
        else if(b == null)
            return a;
        else
            return x -> a.test(x) && b.test(x);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Processes History", req.getRequestURI(), out);
            
            out.append("<h1>Process History</h1>");
            
            List<String> filter = new ArrayList<>();
            List<Predicate<ProcessInfo>> predicates = new ArrayList<>();
            
            String service = req.getParameter("service");
            if(service != null)
            {
                filter.add("service:"+service);
                predicates.add(e -> e.getService().equals(service));
            }
            
            String task = req.getParameter("task");
            if(task != null)
            {
                filter.add("task:"+task);
                predicates.add(e -> e.getName().equals(task));
            }
            
            List<ProcessInfo> tasks = pm.getProcessesHistory();
            Optional<Predicate<ProcessInfo>> predicate = predicates.stream().reduce(ProcessHistoryServlet::combine);
            if(predicate.isPresent())
                tasks = tasks.stream().filter(predicate.get()).collect(Collectors.toList());
            
            if(!filter.isEmpty())
            {
                out.append("<p style=\"border: 1px solid gray; background: #eee; padding: 0.25em\">Filter=");
                out.append(String.join(",", filter));
                
                out.append(" <a class=\"btn btn-secondary\" style=\"width:2em;height:2em;padding:0;font-size:0.5em\" href=\""+URL+"\">x</a>");
                
                out.append("</p>");
            }
            
            list(out, new ReverseList<>(tasks), true, true, "No process history");
            
            Layout.end(out);
        }
    }
    
}
