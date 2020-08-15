package org.exolin.msp.service.pm;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessDataStorage implements ProcessStore
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDataStorage.class);
    
    private static final String CMD = "cmd";
    private static final String TITLE = "title";
    private static final String START_TIME = "startTime";
    
    private final Path directory;

    public ProcessDataStorage(Path directory)
    {
        this.directory = directory;
    }
    
    public ProcessInfo load(String service, String name, Path path) throws IOException
    {
        Properties properties = new Properties();
        List<String> cmd = Arrays.asList(properties.getProperty(CMD).split(" "));
        String title = properties.getProperty(TITLE);
        long startTime = Long.parseLong(properties.getProperty(START_TIME));
        

        return new ProcessInfo(service, name, startTime, null, cmd, title);
    }
    
    public void store(ProcessInfo pi) throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(CMD, String.join(" ", pi.getCmd()));
        properties.setProperty(TITLE, pi.getTitle());
        properties.setProperty(START_TIME, pi.getStartTime()+"");
        
        if(!Files.exists(directory))
            throw new NoSuchFileException(directory.toString());
        
        Path destDir = directory.resolve(pi.getService()).resolve(pi.getName());
        if(!Files.exists(destDir))
            Files.createDirectories(destDir);
        
        try(Writer out = Files.newBufferedWriter(destDir.resolve(pi.getStartTime()+".properties"), StandardCharsets.UTF_8))
        {
            properties.store(out, null);
        }
    }
    
    public List<ProcessInfo> getProcessInfos() throws IOException
    {
        List<ProcessInfo> processInfos = new ArrayList<>();
        
        try(DirectoryStream<Path> serviceDirs = Files.newDirectoryStream(directory))
        {
            for(Path serviceDir: serviceDirs)
            {
                try(DirectoryStream<Path> processDirectories = Files.newDirectoryStream(serviceDir))
                {
                    for(Path processDirectory: processDirectories)
                    {
                        try(DirectoryStream<Path> processFiles = Files.newDirectoryStream(processDirectory))
                        {
                            for(Path processFile: processFiles)
                            {
                                processInfos.add(load(serviceDir.getFileName().toString(), processDirectory.getFileName().toString(), processFile));
                            }
                        }
                    }
                }
            }
        }
        
        return processInfos;
    }

    @Override
    public void add(ProcessInfo processInfo)
    {
        try{
            store(processInfo);
        }catch(IOException e){
            LOGGER.error("Failed to store", e);
        }
    }
}
