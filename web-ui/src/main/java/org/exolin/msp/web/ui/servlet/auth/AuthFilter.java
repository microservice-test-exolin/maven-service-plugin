package org.exolin.msp.web.ui.servlet.auth;

import java.io.IOException;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class AuthFilter implements Filter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
    
    private final GithubOAuth githubOAuth;
    private final Set<String> allowedUsers;

    public AuthFilter(GithubOAuth githubOAuth, Set<String> allowedUsers)
    {
        this.githubOAuth = githubOAuth;
        this.allowedUsers = allowedUsers;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException
    {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException
    {
        doFilter((HttpServletRequest)request, (HttpServletResponse)response, fc);
    }
    
    public static final String USER = "user";
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws IOException, ServletException
    {
        LOGGER.debug("Requesting {}", request.getRequestURI());
        
        //TODO: no exclusion like that for GithubWebhookServlet
        if(request.getRequestURI().startsWith(GithubOAuthServlet.URL) || request.getRequestURI().startsWith(GithubWebhookServlet.URL))
            fc.doFilter(request, response);
        else
        {
            HttpSession session = request.getSession(true);
            String token = (String)session.getAttribute(GithubOAuthServlet.GITHUB_TOKEN);
            if(token == null)
                response.sendRedirect(githubOAuth.getLoginUrl());
            
            String user = githubOAuth.getUser(token).getLogin();
            request.setAttribute(USER, user);
            LOGGER.debug("Auth with {}", user);
            if(allowedUsers.contains(user))
                fc.doFilter(request, response);
            else
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy()
    {
        
    }
}
