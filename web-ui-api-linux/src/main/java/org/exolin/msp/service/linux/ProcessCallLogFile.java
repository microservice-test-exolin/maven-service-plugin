package org.exolin.msp.service.linux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.exolin.msp.service.LogFile;

/**
 * Log file who's content is retrived by calling a command
 * 
 * @author tomgk
 */
public class ProcessCallLogFile extends LogFile
{
    private final String fileName;
    private final String title;
    private final String[] cmd;
    
    private ProcessCallLogFile(String serviceName, String filename, String title, String...cmd)
    {
        super(serviceName, Optional.empty());
        this.cmd = cmd;
        this.fileName = filename;
        this.title = title;
    }
    
    public static ProcessCallLogFile Journalctl(String serviceName)
    {
        return new ProcessCallLogFile(serviceName, "journalctl", "Standard Output", "journalctl", "-u", serviceName);
    }

    @Override
    public String getFileName()
    {
        return fileName;
    }

    @Override
    public String getTitle()
    {
        return title;
    }
    
    private void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] arr = new byte[1024*8];
        int r;
        while((r=in.read(arr)) != -1)
            out.write(arr, 0, r);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException
    {
        Process process = new ProcessBuilder(cmd).start();
        
        try(InputStream in = process.getInputStream())
        {
            copy(in, out);
        }
    }
}
