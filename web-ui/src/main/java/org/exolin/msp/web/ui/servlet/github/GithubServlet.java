package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
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
        resp.setContentType("application/json;charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        GithubPayload payload = mapper.readValue(req.getReader(), GithubPayload.class);
        
        Map<String, String> map = new HashMap<>();
        
        map.put("name", payload.getRepository().getName());

        Service service = null;
        
        try{
            service = services.getServiceFromRepositoryUrl(payload.getRepository().getUrl());
            if(service != null)
                map.put("service", service.getName());
            else
                map.put("service", null);
        }catch(IOException e){
            map.put("error", e.getMessage());
        }
        
        if(service == null)
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        mapper.writeValue(resp.getWriter(), map);
    }
}
