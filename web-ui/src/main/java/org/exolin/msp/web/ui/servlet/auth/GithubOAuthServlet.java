package org.exolin.msp.web.ui.servlet.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class GithubOAuthServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubOAuthServlet.class);
    
    public static final String URL = "/github-oauth";
    
    public static final String GITHUB_TOKEN = "github.token";
    
    private final GithubOAuth githubOAuth;
    
    public GithubOAuthServlet(GithubOAuth githubOAuth)
    {
        this.githubOAuth = githubOAuth;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try{
            String code = request.getParameter("code");
            LOGGER.info("Code={}", code);
            if(code == null)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            request.getSession(true).setAttribute(GITHUB_TOKEN, githubOAuth.getToken(code));
            
            response.sendRedirect("/");
        }catch(GithubAuthException e){
            LOGGER.error("Github auth error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().append("Authentification failed");
        }catch(IOException|RuntimeException e){
            LOGGER.error("Error", e);
            throw e;
        }
    }
}
