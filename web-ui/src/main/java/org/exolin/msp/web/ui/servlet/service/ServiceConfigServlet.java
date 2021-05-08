package org.exolin.msp.web.ui.servlet.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolin.msp.service.ConfigFile;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;
import org.exolin.msp.web.ui.HtmlUtils;
import org.exolin.msp.web.ui.HttpUtils;
import org.exolin.msp.web.ui.servlet.Icon;
import org.exolin.msp.web.ui.servlet.Layout;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.user.User;
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
        try{
            String serviceName = req.getParameter("service");
            if(serviceName == null)
                showServiceList(req, resp);
            else
                showConfigOfService(serviceName, req, resp);
        }catch(IOException|ServletException|RuntimeException e){
            LOGGER.error("Error", e);
            e.printStackTrace();
            throw e;
        }
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
    
    private void writeNonDismissableErrorBox(PrintWriter out, String text)
    {
        writeBox(out, "danger", text, false);
    }
    
    private void writeWarningBox(PrintWriter out, String text)
    {
        writeBox(out, "warning", text, true);
    }
    
    private void writeInfoBox(PrintWriter out, String text)
    {
        writeBox(out, "information", text, true);
    }
    
    private void writeBox(PrintWriter out, String type, String text, boolean dismissable)
    {
        out.append("<div class=\"alert alert-"+type+" alert-dismissible fade show\" role=\"alert\">");
        out.append(text);
        if(dismissable)
        {
            out.append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\">");
            out.append("<span aria-hidden=\"true\">&times;</span>");
            out.append("</button>");
        }
        out.append("</div>");
    }
    
    private void showConfigFile(Service service, String name, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        
        ConfigFile file;
        try{
            file = service.getConfigFile(name);
        }catch(NoSuchFileException e){
            try(PrintWriter out = resp.getWriter())
            {
                String serviceName = service.getName();
                
                Layout.start(serviceName+"/"+getDisplayName(name), req.getRequestURI(), out);

                out.append("<h1>").append(HtmlUtils.escapeHTML(serviceName+"/"+getDisplayName(name))).append("</h1>");
            
                writeNonDismissableErrorBox(out, "File <code>"+HtmlUtils.escapeHTML(name)+"</code> not found");
                
                Layout.end(out);
            }
            return;
        }

        try(PrintWriter out = resp.getWriter())
        {
            String serviceName = service.getName();
            
            Layout.start(serviceName+"/"+getDisplayName(name), req.getRequestURI(), out);
            
            out.append("<h1>").append(HtmlUtils.escapeHTML(serviceName+"/"+getDisplayName(name))).append("</h1>");
            
            writeWarningBox(out, "Changing configuration might require the service to be restarted to take effect");
            writeWarningBox(out, "The values aren't validated and changing them to something invalid might make the service fail to operate");
            
            out.append("<form action=\""+URL+"?service=").append(serviceName).append("&file=").append(name).append("\" method=\"POST\">");
            
            Map<String, String> content = file.get();

            out.append(content.toString());
            
            if(content.isEmpty())
                writeInfoBox(out, "The values aren't validated and changing them to something invalid might make the service fail to operate");
            
            for(Map.Entry<String, String> e: content.entrySet())
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
                {
                    User bot = getDiscordBot(e.getValue());
                    
                    out.append(" Bot: ");
                    if(bot != null)
                    {
                        out.append(bot.getDiscriminatedName());
                        out.append("<br><img style=\"max-width: 100px; max-height: 100px\" src=\"").append(bot.getAvatar().getUrl().toString()).append("\">");
                    }
                    else
                        out.append("<em>unknown</em>");
                }
                
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
        try{
            String serviceName = HttpUtils.getRequiredParameter(req, "service");
            String file = HttpUtils.getRequiredParameter(req, "file");

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

                String val = HttpUtils.getRequiredFormField(req, key);

                if(!isSecret(key) || !val.equals(UNCHANGED_SECRET))
                    configFile.set(key, val);
            }

            configFile.save();
            //Zurück zur Liste
            resp.sendRedirect(URL+"?service="+serviceName);//+"&file="+file);
        }catch(HttpUtils.BadRequestMessage e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    private String getDisplayName(String file)
    {
        if(file.equals("bot.properties"))
            return "Bot configuration";
        else
            return file;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfigServlet.class);
    
    private User getDiscordBot(String token)
    {
        try{
            DiscordApi api = new DiscordApiBuilder()
                    .setToken(token)
                    .login()
                    .get();
            
            return api
                    .getYourself();
        }catch(Exception e){
            LOGGER.error("Error while retriving bot name", e);
            return null;
        }
    }
}
