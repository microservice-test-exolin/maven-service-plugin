package org.exolin.msp.service.sa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;

/**
 *
 * @author tomgk
 */
public class LinuxAbstraction implements SystemAbstraction
{
    @Override
    public void setOwner(Path serviceDir, String serviceUser) throws IOException
    {
        UserPrincipalLookupService lookupService = FileSystems.getDefault()
            .getUserPrincipalLookupService();
        Files.getFileAttributeView(serviceDir, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setOwner(lookupService.lookupPrincipalByName(serviceUser));
    }

    @Override
    public void reloadDeamon() throws IOException
    {
        system("systemctl", "daemon-reload");
    }

    @Override
    public void restart(String name) throws IOException
    {
        system("service", name, "restart");
    }

    @Override
    public void getStatus(String name) throws IOException
    {
        if(!parse(system2("service", name, "status")))
            throw new IllegalStateException("Not started");
    }
    
    boolean parse(String stdout)
    {
        if(stdout.contains("   Active: active"))
            return true;
        else if(stdout.contains("   Active: failed "))
            return false;
        else
            throw new UnsupportedOperationException("Can't parse:\n"+stdout);
    }
    
    private void system(String...cmd) throws IOException
    {
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
    
    protected String system2(String...cmd) throws IOException
    {
        try{
            Process p = new ProcessBuilder(cmd).start();
            
            byte[] b = new byte[1024*8];
            int r;
            
            InputStream in = p.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((r = in.read(b)) != -1)
                out.write(b, 0, r);
            
            if(p.waitFor() != 0)
            {
                InputStream err = p.getInputStream();
                ByteArrayOutputStream errout = new ByteArrayOutputStream();
                while((r = err.read(b)) != -1)
                    errout.write(b, 0, r);
                
                throw new IOException("Failed to execute "+String.join(" ", cmd)+": "+errout.toString());
            }
            
            return new String(out.toByteArray());
        }catch(InterruptedException e){
            InterruptedIOException ex = new InterruptedIOException(e.toString());
            ex.initCause(e);
            throw ex;
        }
    }
}
