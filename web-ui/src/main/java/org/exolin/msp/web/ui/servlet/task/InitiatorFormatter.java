package org.exolin.msp.web.ui.servlet.task;

import org.exolin.msp.web.ui.servlet.github.GithubWebhookServlet;
import org.exolin.msp.web.ui.servlet.service.DeployServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class InitiatorFormatter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorFormatter.class);
    
    public static String displayInitiator(String initiator)
    {
        Initiator i;
        try{
            i = Initiator.parse(initiator);
        }catch(IllegalArgumentException e){
            LOGGER.error("Couldn't parse", e);
            return initiator;
        }
        
        if(i.type.equals(GithubWebhookServlet.INITIATER_PREFIX))
        {
            String repo = i.args.get(GithubWebhookServlet.NAME_REPO);
            String sha1 = i.args.get(GithubWebhookServlet.NAME_SHA1);
            if(repo != null && sha1 != null)
            {
                String shortHash = sha1.substring(0, 7);
                
                return "<a href=\""+repo+"/commit/"+sha1+"\" title=\"Github Webhook - Commit "+shortHash+"\">Commit "+shortHash+"</a>";
            }
            else if(repo != null && sha1 == null)
                return "<a href=\""+repo+"\">Github Webhook</a>";
            else
                return "Github Webhook";
        }
        else if(i.type.equals(DeployServlet.INITIATOR_WEB_UI))
        {
            String user = i.args.get(DeployServlet.NAME_USER);
            if(user != null)
                return "Service Web UI by "+user;
            else
                return "Service Web UI";
        }
        else
            return initiator;
    }
}
