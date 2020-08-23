package org.exolin.msp.web.ui.servlet.cron;

import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author tomgk
 */
public class FetchURLCronJob implements CronjobBody
{
    private final URL url;

    public FetchURLCronJob(URL url)
    {
        this.url = url;
    }

    @Override
    public void execute(Writer out) throws IOException
    {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        
        Tools.dump(con, out);
        
        int response = con.getResponseCode();
        
        if(response / 100 != 2)
            throw new IOException(url+" returned "+response);
    }
}
