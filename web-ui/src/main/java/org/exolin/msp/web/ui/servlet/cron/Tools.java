package org.exolin.msp.web.ui.servlet.cron;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author tomgk
 */
public class Tools
{
    public static void dump(HttpURLConnection con, Writer out) throws IOException
    {
        int response = con.getResponseCode();
        
        InputStream x = response < 300 ? con.getInputStream() : con.getErrorStream();
        Reader in = new InputStreamReader(x, con.getContentEncoding() != null ? con.getContentEncoding() : StandardCharsets.UTF_8.name());
        
        for(int i=0;;++i)
        {
            String value = con.getHeaderField(i);
            if(value == null)
                break;
            
            String key = con.getHeaderFieldKey(i);
            if(key != null)
                out.append(key).append(": ");
            
            out.append(value).append("\r\n");
        }
        
        out.append("\r\n");
        char[] data = new char[1024*8];
        int r;
        while((r=in.read(data))!=-1)
            out.write(data, 0, r);
    }
    
    public static void main(String[] args) throws Exception
    {
        PrintWriter out = new PrintWriter(System.out);
        dump((HttpURLConnection) new URL("https://google.com/").openConnection(), out);
        out.flush();
    }
}
