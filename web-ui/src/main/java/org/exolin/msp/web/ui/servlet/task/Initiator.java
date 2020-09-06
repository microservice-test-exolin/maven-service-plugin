package org.exolin.msp.web.ui.servlet.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tomgk
 */
public class Initiator
{
    public final String type;
    public final Map<String, String> args;

    public Initiator(String type, Map<String, String> args)
    {
        this.type = type;
        this.args = Collections.unmodifiableMap(args);
    }

    public static Initiator parse(String initiator)
    {
        String type;
        Map<String, String> map = new HashMap<>();

        if(!initiator.contains("["))
            type = initiator;
        else if(!initiator.endsWith("]"))
            throw new IllegalArgumentException(initiator);
        else
        {
            int i = initiator.indexOf('[');
            type = initiator.substring(0, i);
            String mapString = initiator.substring(i+1, initiator.length()-1);
            
            //Workaround um zuvor falsch geschriebene Einträge zu parsen
            if(mapString.startsWith("user") && !mapString.contains("="))
            {
                map.put("user", mapString.substring("user".length()));
            }
            else
            {
                String[] pairs = mapString.split(",");
                for(String pair: pairs)
                {
                    int eq = pair.indexOf('=');
                    if(eq == -1)
                        throw new IllegalArgumentException(initiator);
                    map.put(pair.substring(0, eq), pair.substring(eq+1));
                }
            }
        }

        return new Initiator(type, map);
    }

    @Override
    public String toString()
    {
        if(args.isEmpty())
            return type;
        
        return type+args;
    }
}
