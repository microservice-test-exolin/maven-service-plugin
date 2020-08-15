package org.exolin.msp.web.ui.servlet.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.exolin.msp.web.ui.Utils;
import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class GithubOAuth
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubOAuth.class);
    
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper objectMapper = GithubWebhookServlet.createObjectMapper();

    public GithubOAuth(String clientId, String clientSecret)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    
    private HttpURLConnection open(String url) throws IOException
    {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Request {}", url.replace(clientSecret, "${clientSecret}"));
        
        return (HttpURLConnection)new URL(url).openConnection();
    }
    
    public String getToken(String code) throws IOException
    {
        String state = "";
        
        HttpURLConnection con = open("https://github.com/login/oauth/access_token?"
                + "client_id="+clientId+"&"
                + "client_secret="+clientSecret+"&"
                + "code="+code+"&"
                + "state="+state+"&");
        
        String str = Utils.read(new InputStreamReader(con.getInputStream()));
        Map<String, String> query = Utils.splitQuery(str);
        
        String error = query.get("error");
        String error_description = query.get("error_description");
        String access_token = query.get("access_token");
        if(error != null)
            throw new GithubAuthException(error+(error_description != null ? ": "+error_description : ""));
        else if(access_token == null)
            throw new GithubAuthException("token not set: "+query);
        
        return access_token;
    }
    
    public GithubUser getUser(String token) throws IOException
    {
        HttpURLConnection con = (HttpURLConnection) new URL("https://api.github.com/user").openConnection();
        
        con.setRequestProperty("Authorization", "token "+token);
        
        return objectMapper.readValue(con.getInputStream(), GithubUser.class);
    }

    public String getLoginUrl()
    {
        return "https://github.com/login/oauth/authorize?client_id="+clientId;
    }
}
