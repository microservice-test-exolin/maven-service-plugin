package org.exolin.msp.service.pm;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String EXIT_CODE = "exitCode";
    private static final String WORKING_DIRECTORY = "workingDirectory";
    private static final String INITIATOR = "initiator";
    
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
        
        try(Reader in = Files.newBufferedReader(path))
        {
            properties.load(in);
        }
        
        List<String> cmd = Arrays.asList(properties.getProperty(CMD).split(" "));
        long startTime = Long.parseLong(properties.getProperty(START_TIME));
        Long endTime = Optional.ofNullable(properties.getProperty(END_TIME)).map(Long::parseLong).orElse(null);
        Integer exitCode = Optional.ofNullable(properties.getProperty(EXIT_CODE)).map(Integer::parseInt).orElse(null);
        Path workingDirectory = Optional.ofNullable(properties.getProperty(WORKING_DIRECTORY)).map(Paths::get).orElse(null);
        String initiator = properties.getProperty(INITIATOR);
        
        return new ProcessInfo(service, name, startTime, workingDirectory, cmd, initiator, endTime, exitCode);
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
        Path destDir = processDir(pi.getService(), pi.getName());
        
        Path dest = destDir.resolve(pi.getStartTime()+FILE_EXTENSION);
        
        Properties properties = new Properties();
        properties.setProperty(CMD, pi.getCmd());
        properties.setProperty(START_TIME, pi.getStartTime()+"");
        if(pi.getEndTime() != null)
            properties.setProperty(END_TIME, pi.getEndTime()+"");
        if(pi.getExitCode() != null)
            properties.setProperty(EXIT_CODE, pi.getExitCode()+"");
        if(pi.getWorkingDirectory() != null)
            properties.setProperty(WORKING_DIRECTORY, pi.getWorkingDirectory()+"");
        if(pi.getInitiator() != null)
            properties.setProperty(INITIATOR, pi.getInitiator());
        
        LOGGER.info("Saving PI to {}: {}", dest, properties);
        
        try(Writer out = Files.newBufferedWriter(dest, StandardCharsets.UTF_8))
        {
            properties.store(out, null);
        }
    }
    
    public List<ProcessInfo> getProcessInfos()
    {
        List<ProcessInfo> processInfos = new ArrayList<>();
        
        LOGGER.debug("Reading {}", directory);
        
        //<service>/<process>/<startTime>.<ext>
        try(DirectoryStream<Path> serviceDirs = Files.newDirectoryStream(directory.toAbsolutePath().normalize()))
        {
            for(Path serviceDir: serviceDirs)
            {
                LOGGER.debug("Reading {}", serviceDir);
                try(DirectoryStream<Path> processDirectories = Files.newDirectoryStream(serviceDir))
                {
                    for(Path processDirectory: processDirectories)
                    {
                        LOGGER.debug("  Reading {}/*{}", serviceDir.relativize(processDirectory), FILE_EXTENSION);
                        try(DirectoryStream<Path> processFiles = Files.newDirectoryStream(processDirectory, "*"+FILE_EXTENSION))
                        {
                            for(Path processFile: processFiles)
                            {
                                try{
                                    LOGGER.debug("    {}", processFile.getFileName());
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
        
        processInfos.sort(Comparator.comparing(ProcessInfo::getStartTime));
        
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
