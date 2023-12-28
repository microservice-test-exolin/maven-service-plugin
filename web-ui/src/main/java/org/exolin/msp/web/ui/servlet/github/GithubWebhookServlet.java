package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.GitRepository;
import org.exolin.msp.service.GitRepository.Task;
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
    private final ExecutorService executorService;

    public GithubWebhookServlet(Services services, GithubDeployerImpl githubDeployer, ExecutorService executorService)
    {
        this.services = services;
        this.githubDeployer = githubDeployer;
        this.executorService = executorService;
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

            Map<String, GithubDeployerImpl.Repo.Deployment> deployments = new HashMap<>();
            for(Service service: serviceList)
                deployments.put(service.getName(), githubDeployer.fromRepoUrl(service.getGitRepository().get().getRepositoryUrl()).createDeployment(payload.getLatest().getId(), "service "+service.getName()));
            
            map.put("status", "queued");
            executorService.execute(() -> execute(serviceList, payload, deployments));

            mapper.writeValue(resp.getWriter(), map);
        //}catch(IOException|RuntimeException e){
        }catch(Throwable e){
            LOGGER.error("Error in doPost", e);
            throw e;
        }
    }
    
    public static final String INITIATER_PREFIX = "github-webhook";
    public static final String NAME_REPO = "repo";
    public static final String NAME_SHA1 = "sha1";
    
    private void execute(List<Service> serviceList, GithubPayload payload, Map<String, GithubDeployerImpl.Repo.Deployment> deployments)
    {
        String initiator = INITIATER_PREFIX+"["+NAME_REPO+"="+payload.getRepository().getHtml_url();
        if(payload.getLatest() != null)
            initiator += ","+NAME_SHA1+"=" + payload.getLatest().getId();
        initiator += "]";

        Set<Path> build = new HashSet<>();

        try{
            for(Service service: serviceList)
            {
                GitRepository repository = service.getGitRepository().get();
                
                if(repository.supports(Task.BUILD) && repository.supports(Task.DEPLOY))
                {
                    if(build.add(repository.getLocalGitRoot()))
                    {
                        LOGGER.info("Building {} (remote {})", repository.getLocalGitRoot(), repository.getRepositoryUrl());
                        repository.run(Task.BUILD, false, initiator);
                    }

                    LOGGER.info("Deploying {}", service.getName());
                    repository.run(Task.DEPLOY, false, initiator);
                }
                else if(repository.supports(Task.BUILD_AND_DEPLOY))
                {
                    LOGGER.info("Building and deploying {}", service.getName());
                    repository.run(Task.BUILD_AND_DEPLOY, false, initiator);
                }
                else
                    throw new UnsupportedOperationException("Neither BUILD+DEPLOY nor BUILD_AND_DEPLOY are supported by "+repository);

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
            LOGGER.error("Failed to build/deploy", e);
        }

        //set all non finished to error
        try{
            for(GithubDeployerImpl.Repo.Deployment dep: deployments.values())
                dep.createDeploymentStatus(GithubDeployerImpl.DeploymentStatus.error, null);
        }catch(IOException e){
            LOGGER.error("Failed to set deployment status", e);
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
