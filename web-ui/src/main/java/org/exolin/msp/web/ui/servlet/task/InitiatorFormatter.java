package org.exolin.msp.web.ui.servlet.task;

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
        
        if(i.type.equals("github-webhook"))
        {
            String repo = i.args.get("repo");
            String sha1 = i.args.get("sha1");
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
        if(i.type.equals("service-web-ui"))
        {
            String user = i.args.get("user");
            if(user != null)
                return "Service Web UI by "+user;
            else
                return "Service Web UI";
        }
        else
            return initiator;
    }
}
