package org.exolin.msp.web.ui.servlet.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tomgk
 */
public class GithubLogoutServlet extends HttpServlet
{
    public static final String URL = "/sign-out";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.getWriter().append("Not supported yet");
        //req.getSession(true).removeAttribute(GithubOAuthServlet.GITHUB_TOKEN);
        //resp.sendRedirect("/");
    }
}
