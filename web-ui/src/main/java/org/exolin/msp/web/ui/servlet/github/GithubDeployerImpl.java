package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class GithubDeployerImpl implements GithubDeployer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Repo.class);
    
    private final String token;
    private final ObjectMapper mapper = GithubServlet.createObjectMapper();

    public GithubDeployerImpl(String token)
    {
        this.token = token;
    }
    
    private HttpURLConnection openConnection(String method, String url) throws IOException
    {
        LOGGER.info("{} {}", method, url);
        
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(!method.equalsIgnoreCase("GET"));
        con.setRequestProperty("Accept", "application/vnd.github.v3+json");
        con.setRequestProperty("Authorization", "token "+token);
        return con;
    }
    
    private void send(HttpURLConnection con, Map<String, String> body) throws IOException
    {
        LOGGER.info("Sending ", mapper.writeValueAsString(body));
        mapper.writeValue(con.getOutputStream(), body);
    }
    
    public Repo fromRepoUrl(String repoUrl)
    {
        //https://github.com/microservice-test-exolin/maven-service-plugin/issues/35
        String prefix = "https://github.com/";
        if(!repoUrl.startsWith(prefix))
            throw new IllegalArgumentException(repoUrl);
        
        repoUrl = repoUrl.substring(prefix.length());
        if(repoUrl.endsWith("/"))
            repoUrl = repoUrl.substring(0, repoUrl.length()-1);
        
        String[] parts = repoUrl.split("/");
        if(parts.length != 2)
            throw new IllegalArgumentException(repoUrl);
        
        return new Repo(parts[0], parts[1]);
    }
    
    public class Repo
    {
        private final String owner;
        private final String repo;

        public Repo(String owner, String repo)
        {
            this.owner = owner;
            this.repo = repo;
        }

        public Deployment createDeployment(String ref, String environment) throws IOException
        {
            HttpURLConnection con = openConnection("POST", "https://api.github.com/repos/"+owner+"/"+repo+"/deployments");

            Map<String, String> body = new HashMap<>();
            body.put("ref", ref);
            body.put("environment", environment);

            send(con, body);
            
            checkSuccess(con);

            return new Deployment(mapper.readValue(con.getInputStream(), CreatedDeployment.class).getId());
        }
        
        public class Deployment
        {
            private final long deploymentId;

            public Deployment(long deploymentId)
            {
                this.deploymentId = deploymentId;
            }
        
            private HttpURLConnection openDeployment(String method) throws IOException
            {
                return openConnection(method, "https://api.github.com/repos/"+owner+"/"+repo+"/deployments/"+deploymentId);
            }
            
            public void createDeploymentStatus(DeploymentStatus state) throws IOException
            {
                createDeploymentStatus(state, null);
            }
            
            public void createDeploymentStatus(DeploymentStatus state, String environmentUrl) throws IOException
            {
                HttpURLConnection con = openConnection("POST", "https://api.github.com/repos/"+owner+"/"+repo+"/deployments/"+deploymentId+"/statuses");

                Map<String, String> body = new HashMap<>();
                body.put("state", state.toString());
                body.put("auto_inactive", "true");
                //body.put("log_url", "http://example.org/log/"+deploymentId);
                //body.put("target", "http://example.org/target/"+deploymentId);
                if(environmentUrl != null)
                    body.put("environment_url", environmentUrl);

                send(con, body);

                checkSuccess(con);
            }

            public void deleteDeployment(long id) throws IOException
            {
                HttpURLConnection con = openDeployment("DELETE");
                checkSuccess(con);
            }

            private long getDeploymentId()
            {
                return deploymentId;
            }
        }
    }
    
    private void checkSuccess(HttpURLConnection con) throws IOException
    {
        if(con.getResponseCode()/100 == 2)
            return;
        
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        int r;
        while((r = con.getErrorStream().read()) != -1)
            arr.write(r);
        
        LOGGER.error("Returned "+con.getResponseCode()+"\n"+new String(arr.toByteArray()));
        
        throw new IOException("Returned "+con.getResponseCode());
    }
    
    public enum DeploymentStatus
    {
        success,
        error,
        in_progress,
        queued,
        pending
    }
    
    public static void main(String[] args) throws MalformedURLException, IOException
    {
        GithubDeployerImpl github = new GithubDeployerImpl(new String(Files.readAllBytes(Paths.get("config/github.token"))));
        
        String owner = "microservice-test-exolin";
        String repo = "maven-service-plugin";
        
        Repo rep = github.new Repo(owner, repo);
        
        Repo.Deployment dep = rep.createDeployment("5d7f23a39603e2c505924e21654ae12a815610b5", "service deployment");
        dep.createDeploymentStatus(DeploymentStatus.success, "http://example.org/environment/"+dep.getDeploymentId());
    }
}
