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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessDataStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDataStorage.class);
    
    private static final String CMD = "cmd";
    private static final String TITLE = "title";
    private static final String START_TIME = "startTime";
    private static final String EXIT_CODE = "exitCode";
    
    private final Path directory;
    
    private static final String FILE_EXTENSION = ".properties";

    public ProcessDataStorage(Path directory) throws NoSuchFileException
    {
        this.directory = directory;
        
        if(!Files.exists(directory))
            throw new NoSuchFileException(directory.toAbsolutePath().normalize().toString());
    }
    
    public ProcessInfo load(String service, String name, Path path) throws IOException
    {
        Properties properties = new Properties();
        List<String> cmd = Arrays.asList(properties.getProperty(CMD).split(" "));
        String title = properties.getProperty(TITLE);
        long startTime = Long.parseLong(properties.getProperty(START_TIME));
        Integer exitCode = Optional.ofNullable(properties.getProperty(EXIT_CODE)).map(Integer::parseInt).orElse(null);
        
        return new ProcessInfo(service, name, startTime, cmd, title, exitCode);
    }

    public Map<String, Path> getProcessLogDirectories(String service) throws IOException
    {
        Path serviceDir = directory.resolve(service);
        if(!Files.exists(serviceDir))
        {
            LOGGER.warn("Directory doesn't exist: {}", serviceDir);
            return new HashMap<>();
        }
        
        Map<String, Path> dirs = new HashMap<>();
        try(DirectoryStream<Path> processes = Files.newDirectoryStream(serviceDir))
        {
            for(Path processDir: processes)
                dirs.put(processDir.getFileName().toString(), processDir);
        }
        return dirs;
    }
    
    private Path processDir(String service, String process) throws IOException
    {
        if(!Files.exists(directory))
            throw new NoSuchFileException(directory.toString());
        
        Path destDir = directory.resolve(service).resolve(process);
        if(!Files.exists(destDir))
            Files.createDirectories(destDir);
        
        return destDir;
    }
    
    public Path getLogFile(ProcessInfo pi) throws IOException
    {
        return processDir(pi.getService(), pi.getName()).resolve(pi.getStartTime()+".log");
    }
    
    public void store(ProcessInfo pi) throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(CMD, String.join(" ", pi.getCmd()));
        properties.setProperty(TITLE, pi.getTitle());
        properties.setProperty(START_TIME, pi.getStartTime()+"");
        
        Path destDir = processDir(pi.getService(), pi.getName());
        
        try(Writer out = Files.newBufferedWriter(destDir.resolve(pi.getStartTime()+FILE_EXTENSION), StandardCharsets.UTF_8))
        {
            properties.store(out, null);
        }
    }
    
    public List<ProcessInfo> getProcessInfos()
    {
        List<ProcessInfo> processInfos = new ArrayList<>();
        
        LOGGER.info("Reading {}", directory);
        
        //<service>/<process>/<startTime>.<ext>
        try(DirectoryStream<Path> serviceDirs = Files.newDirectoryStream(directory))
        {
            for(Path serviceDir: serviceDirs)
            {
                LOGGER.info("Reading {}", serviceDir);
                try(DirectoryStream<Path> processDirectories = Files.newDirectoryStream(serviceDir))
                {
                    for(Path processDirectory: processDirectories)
                    {
                        LOGGER.info("Reading {}/*{}", processDirectory, FILE_EXTENSION);
                        try(DirectoryStream<Path> processFiles = Files.newDirectoryStream(processDirectory, "*"+FILE_EXTENSION))
                        {
                            for(Path processFile: processFiles)
                            {
                                try{
                                    LOGGER.info("  {}", processFile);
                                    processInfos.add(load(serviceDir.getFileName().toString(), processDirectory.getFileName().toString(), processFile));
                                }catch(IOException|RuntimeException e){
                                    LOGGER.error("Error while reading "+processFile, e);
                                }
                            }
                        }catch(IOException e){
                            LOGGER.error("Error while reading {}", processDirectory);
                        }
                    }
                }catch(IOException e){
                    LOGGER.error("Error while reading {}", serviceDir);
                }
            }
        }catch(IOException e){
            LOGGER.error("Error while reading {}", directory);
        }
        
        return processInfos;
    }

    public void save(ProcessInfo processInfo)
    {
        try{
            store(processInfo);
        }catch(IOException e){
            LOGGER.error("Failed to store", e);
        }
    }
}
