package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ServiceConfigServlet extends HttpServlet
{
    public static final String URL = "/services/config";
    
    private final Services services;

    public ServiceConfigServlet(Services services)
    {
        this.services = services;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
            showServiceList(req, resp);
        else
            showConfigOfService(serviceName, req, resp);
    }
    
    private void showServiceList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Configs", req.getRequestURI(), out);
            
            out.append("<h1>Configs</h1>");
            
            for(Service service: services.getServices())
            {
                int count = service.getConfigFiles().size();
                
                if(count != 0)
                    out.append("<a href=\""+URL+"?service=").append(service.getName()).append("\">");
                
                out.append(service.getName()).append(" (").append(count+"").append(" config file"+(count != 1 ? "s" : "")+")");
                
                if(count != 0)
                    out.append("</a>");
                
                out.append("<br>");
            }
            
            Layout.end(out);
        }
    }
    
    private void showConfigOfService(String serviceName, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Service service = services.getService(serviceName);
        if(service == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service not found");
            return;
        }

        String file = req.getParameter("file");
        if(file != null)
            showConfigFile(service, file, req, resp);
        else
            listConfigFiles(service, req, resp);
    }
    
    private void listConfigFiles(Service service, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        String serviceName = service.getName();
        
        try(PrintWriter out = resp.getWriter())
        {
            Layout.start("Config of "+serviceName, req.getRequestURI(), out);
            
            out.append("<h1>Config of "+serviceName+"</h1>");
            
            for(String file: service.getConfigFiles())
            {
                out.append("<a href=\""+URL+"?service="+serviceName+"&file="+file+"\">");
                getIcon(file).writeTo(out);
                out.append(getDisplayName(file)).append("</a><br>");
            }
            
            //out.append("</div>");
            Layout.end(out);
        }
    }
    
    private Icon getIcon(String name)
    {
        if(name.equals("database.properties"))
            return Icon.DATABASE;
        else if(name.equals("bot.properties") || name.endsWith(".token"))
            return Icon.SHARE;
        else
            return Icon.SETTINGS;
    }
    
    private static final String UNCHANGED_SECRET = "<unchanged>";
    private boolean isSecret(String name)
    {
        return name.equals("password") ||
               name.endsWith(".password") ||
               name.endsWith("_secret");
    }
    
    private void showConfigFile(Service service, String name, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        try(PrintWriter out = resp.getWriter())
        {
            String serviceName = service.getName();
            
            Layout.start(serviceName+"/"+getDisplayName(name), req.getRequestURI(), out);
            
            out.append("<h1>").append(serviceName+"/"+getDisplayName(name)).append("</h1>");
            
            out.append("<div class=\"alert alert-warning\" role=\"alert\">");
            out.append("Changing configuration might require the service to be restarted to take effect");
            out.append("</div>");
            
            out.append("<div class=\"alert alert-warning\" role=\"alert\">");
            out.append("The values aren't validated and changing them to something invalid might make the service fail to operate");
            out.append("</div>");
            
            ConfigFile file = service.getConfigFile(name);

            out.append("<form action=\""+URL+"?service=").append(serviceName).append("&file=").append(name).append("\" method=\"POST\">");

            for(Map.Entry<String, String> e: file.get().entrySet())
            {
                out.append("<div class=\"form-group\">");

                out.append("<label for=\"").append(e.getKey()).append("\">");
                out.append(e.getKey());
                out.append("</label>");

                out.append(": <input class=\"form-control\" name=\"").append(e.getKey()).append("\" ");
                if(isSecret(e.getKey()))
                    out.append("value=\"").append(UNCHANGED_SECRET).append("\"").append(" type=\"password\"").append(" onclick=\"if(this.value=='").append(UNCHANGED_SECRET).append("')this.select();\"");
                else
                    out.append("value=\"").append(e.getValue()).append("\"");
                out.append(">");
                
                if(e.getKey().equals("discord.apiKey"))
                    out.append(" Bot: ").append(getDiscordBotName(e.getValue()));
                
                out.append("<br>");

                out.append("</div>");
            }

            out.append("<button class=\"btn btn-primary\" type=\"submit\">Save</button>");

            out.append("</form>");
            
            Layout.end(out);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String serviceName = req.getParameter("service");
        if(serviceName == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter service");
            return;
        }
        String file = req.getParameter("file");
        if(file == null)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter file");
            return;
        }
        
        Service service = services.getService(serviceName);
        if(service == null)
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Service not found");
            return;
        }
        
        ConfigFile configFile = service.getConfigFile(file);
        for(String key: configFile.get().keySet())
        {
            if(key.equals("service") || key.equals("file"))  //könnte in query sein
                throw new UnsupportedOperationException("ambiguity");
            
            String val = req.getParameter(key);
            if(val == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing form field "+key);
                return;
            }
            
            if(!isSecret(key) || !val.equals(UNCHANGED_SECRET))
                configFile.set(key, val);
        }
        
        configFile.save();
        //Zurück zur Liste
        resp.sendRedirect(URL+"?service="+serviceName);//+"&file="+file);
    }
    
    private String getDisplayName(String file)
    {
        if(file.equals("bot.properties"))
            return "Bot configuration";
        else
            return file;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfigServlet.class);
    
    private String getDiscordBotName(String token)
    {
        try{
            DiscordApi api = new DiscordApiBuilder()
                    .setToken(token)
                    .login()
                    .get();
            
            return api
                    .getYourself()
                    .getDiscriminatedName();
        }catch(Exception e){
            LOGGER.error("Error while retriving bot name", e);
            return "<em>unknown</em>";
        }
    }
}
