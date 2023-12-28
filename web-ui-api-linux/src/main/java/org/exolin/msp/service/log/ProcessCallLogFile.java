package org.exolin.msp.service.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.exolin.msp.service.LogFile;

/**
 * Log file which content is retrived by calling a command
 * 
 * @author tomgk
 */
public class ProcessCallLogFile extends LogFile
{
    private final String fileName;
    private final String title;
    private final String[] cmd;
    
    public ProcessCallLogFile(String serviceName, String filename, String title, String []cmd)
    {
        super(serviceName, Optional.empty());
        this.cmd = cmd;
        this.fileName = filename;
        this.title = title;
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
