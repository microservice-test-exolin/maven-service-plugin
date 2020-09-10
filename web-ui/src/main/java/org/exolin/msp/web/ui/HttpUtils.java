package org.exolin.msp.web.ui;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author tomgk
 */
public class HttpUtils
{
    public static class BadRequestMessage extends Exception
    {
        public BadRequestMessage(String message)
        {
            super(message);
        }
    }
    
    public static String getRequiredParameter(HttpServletRequest req, String name) throws BadRequestMessage
    {
        String value = req.getParameter(name);
        
        if(value == null)
            throw new BadRequestMessage("missing parameter "+name);
        
        return value;
    }

    public static String getRequiredFormField(HttpServletRequest req, String key) throws BadRequestMessage
    {
        try{
            return getRequiredParameter(req, key);
        }catch(BadRequestMessage e){
            throw new BadRequestMessage("missing form field "+key);
        }
    }
}
