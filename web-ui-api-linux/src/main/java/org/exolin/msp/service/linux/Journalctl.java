package org.exolin.msp.service.linux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.exolin.msp.service.LogFile;

/**
 *
 * @author tomgk
 */
public class Journalctl extends LogFile
{
    public Journalctl(String serviceName)
    {
        super(serviceName, Optional.empty());
    }

    @Override
    public String getFileName()
    {
        return "journalctl";
    }

    @Override
    public String getTitle()
    {
        return "Standard Output";
    }

    @Override
    public void writeTo(OutputStream out) throws IOException
    {
        Process process = new ProcessBuilder("journalctl", "-u", getServiceName())
                .start();
        
        try(InputStream in = process.getInputStream())
        {
            byte[] arr = new byte[1024*8];
            int r;
            while((r=in.read(arr)) != -1)
                out.write(arr, 0, r);
        }
    }
}
