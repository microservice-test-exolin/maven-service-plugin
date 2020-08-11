package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;

/**
 *
 * @author tomgk
 */
public class GithubServlet extends HttpServlet
{
    private final Services services;

    public GithubServlet(Services services)
    {
        this.services = services;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        GithubPayload payload = mapper.readValue(req.getReader(), GithubPayload.class);
        
        Writer out = resp.getWriter();
        out.append("{");
        out.append("{\"name\": \"").append(payload.getRepository().getName()).append("\"");
        out.append(",");
        
        out.append("\"service\": ");
        try{
            Service service = services.getServiceFromRepositoryUrl(payload.getRepository().getUrl());
            out.append("\"").append(service.getName()).append("\"");
        }catch(IOException e){
            out.append("null, error: \"").append(e.getMessage()).append("\"");
        }
        
        out.append("}");
    }
}
