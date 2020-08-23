package org.exolin.msp.web.ui.servlet.task;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Processes History", req.getRequestURI(), out);
            
            out.append("<h1>Process History</h1>");
            
            list(out, new ReverseList<>(pm.getProcessesHistory()), true, true, "No process history");
            
            Layout.end(out);
        }
    }
    
}
