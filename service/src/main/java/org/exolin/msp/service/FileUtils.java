package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.maven.plugin.logging.Log;
import org.exolin.msp.service.sa.SystemAbstraction;

/**
 *
 * @author tomgk
 */
public class FileUtils
{
    private final Log log;
    private final String user;
    private final SystemAbstraction sys;

    public FileUtils(Log log, String user, SystemAbstraction sys)
    {
        this.log = log;
        this.user = user;
        this.sys = sys;
    }

    private void _copy(Path src, Path dest) throws IOException
    {
        try{
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
        
        sys.setOwner(dest, user);
    }
    
    public void copy(Path src, Path dest) throws IOException
    {
        try{
            log.info("Copy "+src+" to "+dest);
            _copy(src, dest);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
    }
    
    public void copyDirectoryContent(Path src, Path dest) throws IOException
    {
        log.info("Copy "+src+"/* to "+dest);
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(src))
        {
            for(Path f: dir)
            {
                log.info("  Copy "+src.relativize(f));
                
                _copy(f, dest.resolve(f.getFileName()));
            }
        }
    }

    public void createDirectories(Path dir) throws IOException
    {
        log.info("Create directory "+dir);
        Files.createDirectories(dir);
        sys.setOwner(dir, user);
    }
}
