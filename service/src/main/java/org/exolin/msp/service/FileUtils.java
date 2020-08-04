package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author tomgk
 */
public class FileUtils
{
    private static void _copy(Path src, Path dest) throws IOException
    {
        try{
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
    }

    /*public static void copyIntoDirectory(Log log, Path src, Path destDirectory) throws IOException
    {
        copy(log, src, destDirectory.resolve(src.getFileName()));
    }*/
    
    public static void copy(Log log, Path src, Path dest) throws IOException
    {
        try{
            log.info("Copy "+src+" to "+dest);
            _copy(src, dest);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
    }
    
    public static void copyDirectoryContent(Log log, Path src, Path dest) throws IOException
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

    public static void createDirectories(Log log, Path dir) throws IOException
    {
        log.info("Create directory "+dir);
        Files.createDirectories(dir);
    }
}
