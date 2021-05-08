package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.exolin.msp.core.StatusInfo;

/**
 *
 * @author tomgk
 */
public interface Service
{
    public String getName();
    public ApplicationInstance getApplicationInstance();
    
    /**
     * Returns all log files of the service itself
     * 
     * @return all log files of the service excluding task log files
     * @throws IOException 
     */
    public Map<String, LogFile> getServiceLogFiles() throws IOException;
    
    /**
     * Returns all log files of the task to the service
     * 
     * @param taskName the task
     * @return the log files to the task
     * @throws IOException 
     */
    public Map<String, LogFile> getTaskLogFiles(String taskName) throws IOException;

    public Optional<GitRepository> getGitRepository() throws IOException;

    public Iterable<String> getTasks();
    
    /**
     * Returns the names of the config files of the service.
     * The names are service specific and there might not be any.
     * 
     * @return the names of the config files
     * @throws IOException in case of an I/O error
     */
    public List<String> getConfigFiles() throws IOException;
    
    /**
     * Returns the config file of the service with the name.
     * 
     * @param name the name of the config file
     * @return the config file
     * @throws IOException in case of an I/O error
     */
    public ConfigFile getConfigFile(String name) throws IOException;
}
