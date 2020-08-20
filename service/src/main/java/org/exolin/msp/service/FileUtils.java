package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;
import org.exolin.msp.core.SystemAbstraction;

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
    
    public void replaceDirectoryContent(Path src, Path dest, Set<Path> keep) throws IOException
    {
        log.info("Copy "+src+"/* to "+dest);
        Set<String> found = new HashSet<>();
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(src))
        {
            for(Path f: dir)
            {
                log.info("  Copy "+src.relativize(f));
                
                _copy(f, dest.resolve(f.getFileName()));
                found.add(f.getFileName().toString());
            }
        }
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(dest))
        {
            for(Path f: dir)
            {
                if(!found.contains(f.getFileName().toString()) && !keep.contains(f))
                {
                    log.info("  Delete old "+dest.relativize(f));
                    Files.delete(f);
                }
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
