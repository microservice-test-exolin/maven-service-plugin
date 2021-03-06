package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 *
 * @author tomgk
 */
public class ResourceServlet extends HttpServlet
{
    private final String contentType;
    private final String path;

    public ResourceServlet(String contentType, String path)
    {
        this.contentType = contentType;
        this.path = path;
    }
    
    public static void addFile(ServletHandler servletHandler, String filename, String contentType)
    {
        servletHandler.addServletWithMapping(ResourceServlet.class, "/"+filename).setServlet(new ResourceServlet(contentType, "files/"+filename));
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType(contentType);
        InputStream in = ResourceServlet.class.getClassLoader().getResourceAsStream(path);
        if(in == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try{
            byte[] arr = new byte[1024];
            int r;
            OutputStream out = resp.getOutputStream();
            while((r = in.read(arr)) != -1)
                out.write(arr, 0, r);
        }finally{
            in.close();
        }
    }
}
