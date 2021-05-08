package org.exolin.msp.web.ui.servlet.log;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.HttpUtils;

/**
 *
 * @author tomgk
 */
public class TaskLogFileShowServlet extends LogFileShowServlet
{
    public static final String URL = "/tasks/logfile";
    
    private final Services services;

    public TaskLogFileShowServlet(Services services)
    {
        this.services = services;
    }

    public static String getFileUrl(String service, String taskName, String logfile)
    {
        return LogFileShowServlet.getFileUrl(service, Optional.of(taskName), logfile, false);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            String serviceName = HttpUtils.getRequiredParameter(req, SERVICE);
            String taskName = HttpUtils.getRequiredParameter(req, TASK);

            Service service = services.getService(serviceName);
            if(service == null)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service "+serviceName+" not found");
                return;
            }

            String logFile = req.getParameter(LOGFILE);
            LogFile lf = service.getTaskLogFiles(taskName).get(logFile);
            boolean raw = isRawRequest(req);
            showLogFile(service, Optional.of(taskName), logFile, lf, raw, req, resp);
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
