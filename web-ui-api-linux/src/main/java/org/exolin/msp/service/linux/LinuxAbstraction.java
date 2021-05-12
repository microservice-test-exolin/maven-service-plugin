package org.exolin.msp.service.linux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import org.exolin.msp.core.Log;
import org.exolin.msp.core.SystemAbstraction;
import org.exolin.msp.service.ApplicationInstance;

/**
 *
 * @author tomgk
 */
public class LinuxAbstraction implements SystemAbstraction
{
    private final Log log;

    public LinuxAbstraction(Log log)
    {
        this.log = log;
    }

    @Override
    public ApplicationInstance getNativeService(String name)
    {
        return new LinuxServiceApplicationInstance(name);
    }
    
    @Override
    public void setOwner(Path path, String user) throws IOException
    {
        log.info("Setting owner for "+path+" to "+user);
        
        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setOwner(lookupService.lookupPrincipalByName(user));
    }

    @Override
    public void reloadDeamon() throws IOException
    {
        system(log, "systemctl", "daemon-reload");
    }

    public static void system(Log log, String...cmd) throws IOException
    {
        log.info("$>"+String.join(" ", cmd));
        
        try{
            //getLog().info("> Pseudo "+String.join(" ", cmd));
            if(new ProcessBuilder(cmd).start().waitFor() != 0)
                throw new IOException("Failed to execute "+String.join(" ", cmd));
        }catch(InterruptedException e){
            InterruptedIOException ex = new InterruptedIOException(e.toString());
            ex.initCause(e);
            throw ex;
        }
    }
    
    public static String system2(Log log, String...cmd) throws IOException
    {
        log.info("$>"+String.join(" ", cmd));
        
        try{
            Process p = new ProcessBuilder(cmd).start();
            
            String str = read(p);
            log.info(str);
            return str;
        }catch(InterruptedException e){
            InterruptedIOException ex = new InterruptedIOException(e.toString());
            ex.initCause(e);
            throw ex;
        }
    }
    
    public static String[] createBashExecutionCmd(String line)
    {
        return new String[]{"/bin/bash", "-c", line};
    }
    
    public static String read(Process p) throws IOException, InterruptedException
    {
        byte[] b = new byte[1024*8];
        int r;

        InputStream in = p.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while((r = in.read(b)) != -1)
            out.write(b, 0, r);
        
        InputStream err = p.getInputStream();
        ByteArrayOutputStream errout = new ByteArrayOutputStream();
        while((r = err.read(b)) != -1)
            errout.write(b, 0, r);

        p.waitFor();
        /*if(false)//p.waitFor() != 0)
        {
            InputStream err = p.getInputStream();
            ByteArrayOutputStream errout = new ByteArrayOutputStream();
            while((r = err.read(b)) != -1)
                errout.write(b, 0, r);

            throw new IOException(String.join(" ", cmd)+" exited with "+p.exitValue()+": "+errout.toString()+out.toString());
        }*/

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
