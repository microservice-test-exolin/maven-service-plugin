package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tomgk
 */
public class FaviconServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("image/png");
        InputStream in = FaviconServlet.class.getClassLoader().getResourceAsStream("files/favicon.png");
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
