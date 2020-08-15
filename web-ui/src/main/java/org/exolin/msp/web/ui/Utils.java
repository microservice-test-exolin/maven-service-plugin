package org.exolin.msp.web.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author tomgk
 */
public class Utils
{
    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException
    {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs)
        {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
    
    public static String read(Reader in) throws IOException
    {
        StringWriter out = new StringWriter();
        
        int r;
        while((r=in.read())!=-1)
            out.write(r);
        
        return out.toString();
    }
}
