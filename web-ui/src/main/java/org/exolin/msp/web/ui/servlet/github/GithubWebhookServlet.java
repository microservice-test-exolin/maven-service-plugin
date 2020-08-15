package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
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
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.github.api.GithubDeployerImpl;
import org.exolin.msp.web.ui.servlet.github.api.GithubPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class GithubWebhookServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubWebhookServlet.class);
    
    public static final String URL = "/github";
    
    private final Services services;
    private final GithubDeployerImpl githubDeployer;

    public GithubWebhookServlet(Services services, GithubDeployerImpl githubDeployer)
    {
        this.services = services;
        this.githubDeployer = githubDeployer;
    }
    
    public static ObjectMapper createObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try{
            resp.setContentType("application/json;charset=UTF-8");

            ObjectMapper mapper = createObjectMapper();

            GithubPayload payload = mapper.readValue(req.getReader(), GithubPayload.class);

            Map<String, Object> map = new HashMap<>();

            map.put("name", payload.getRepository().getName());
            
            List<Service> serviceList = getServices(req.getParameterValues("service"), payload.getRepository().getHtml_url());
            map.put("services", serviceList.stream().map(Service::getName).collect(Collectors.toList()));

            Set<String> build = new HashSet<>();
            
            Map<String, GithubDeployerImpl.Repo.Deployment> deployments = new HashMap<>();
            for(Service service: serviceList)
                deployments.put(service.getName(), githubDeployer.fromRepoUrl(service.getRepositoryUrl()).createDeployment(payload.getLatest().getId(), "service "+service.getName()));
            
            String error = null;
            try{
                for(Service service: serviceList)
                {
                    if(build.add(service.getRepositoryUrl()))
                    {
                        LOGGER.info("Building {}", service.getRepositoryUrl());
                        service.build(false);
                    }
                    
                    LOGGER.info("Deploying {}", service.getName());
                    service.deploy(false);
                    
                    GithubDeployerImpl.Repo.Deployment deployment = deployments.get(service.getName());
                    if(deployment != null)
                    {
                        deployment.createDeploymentStatus(GithubDeployerImpl.DeploymentStatus.success, null);
                        deployments.remove(service.getName());
                    }
                    else
                        LOGGER.error("No deployment for {}", service.getName());
                }
            }catch(IOException|InterruptedException e){
                error = e.getMessage();
            }
                
            //set all non finished to error
            for(GithubDeployerImpl.Repo.Deployment dep: deployments.values())
                dep.createDeploymentStatus(GithubDeployerImpl.DeploymentStatus.error, null);

            if(error != null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                map.put("error", error);
            }

            mapper.writeValue(resp.getWriter(), map);
        //}catch(IOException|RuntimeException e){
        }catch(Throwable e){
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
            List<Service> serviceList = services.getServicesFromRepositoryUrl(url);
            if(serviceList.isEmpty())
                throw new RuntimeException("Service not found for "+url);
            
            return serviceList;
        }
    }
}