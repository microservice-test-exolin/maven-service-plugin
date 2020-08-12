package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;
import org.exolin.msp.web.ui.linux.LinuxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class GithubServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubServlet.class);
    
    private final Services services;

    public GithubServlet(Services services)
    {
        this.services = services;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            resp.setContentType("application/json;charset=UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            GithubPayload payload = mapper.readValue(req.getReader(), GithubPayload.class);

            Map<String, Object> map = new HashMap<>();

            map.put("name", payload.getRepository().getName());
            
            List<Service> serviceList = getServices(req.getParameterValues("service"), payload.getRepository().getHtml_url());
            map.put("services", serviceList.stream().map(Service::getName).collect(Collectors.toList()));

            Set<String> build = new HashSet<>();
            
            String error = null;
            try{
                for(Service service: serviceList)
                {
                    if(build.add(service.getRepositoryUrl()))
                        service.build(false);
                    
                    service.deploy(false);
                }
            }catch(IOException|InterruptedException e){
                error = e.getMessage();
            }

            if(error != null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                map.put("error", error);
            }

            mapper.writeValue(resp.getWriter(), map);
        }catch(IOException|RuntimeException e){
            LOGGER.error("Error in doPost", e);
            throw e;
        }
    }
    
    private List<Service> getServices(String[] serviceNames, String url) throws IOException
    {
        if(serviceNames != null)
        {
            List<Service> serviceList = new ArrayList<>();
            for(String serviceName: serviceNames)
            {
                Service service = services.getService(serviceName);
                if(service == null)
                    throw new RuntimeException("Service not found for "+serviceName);
                
                serviceList.add(service);
            }
            return serviceList;
        }
        else
        {
            Service service = services.getServiceFromRepositoryUrl(url);
            if(service == null)
                throw new RuntimeException("Service not found for "+url);
            
            return Collections.singletonList(service);
        }
    }
}
