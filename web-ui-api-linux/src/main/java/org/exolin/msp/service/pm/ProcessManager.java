package org.exolin.msp.service.pm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all created processes for their life time
 * 
 * @author tomgk
 */
public class ProcessManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);
    private final ProcessDataStorage store;
    private final List<ProcessInfo> processes = new ArrayList<>();
    private final List<ProcessInfo> processesHistory;

    public ProcessManager(ProcessDataStorage store)
    {
        this.store = store;
        this.processesHistory = store.readProcessInfos();
    }

    public synchronized ProcessInfo register(String service, String name, List<String> cmd, Path workingDirectory, long startTime, String initiator)
    {
        clean();
        ProcessInfo pi = new ProcessInfo(service, name, startTime, workingDirectory.toAbsolutePath().normalize(), cmd, initiator, null, null);
        processes.add(pi);
        store.saveProcessInfo(pi);
        return pi;
    }
    
    public Path getLogFile(ProcessInfo pi) throws IOException
    {
        return store.getLogFile(pi);
    }

    public synchronized List<ProcessInfo> getProcesses()
    {
        clean();
        return new ArrayList<>(processes);
    }

    public synchronized List<ProcessInfo> getProcessesHistory()
    {
        clean();
        return new ArrayList<>(processesHistory);
    }

    public synchronized List<ProcessInfo> getProcessesIncludingHistory(String service)
    {
        clean();
        List<ProcessInfo> list = new ArrayList<>();
        Consumer<ProcessInfo> consumer = pi -> {
            if(pi.getService().equals(service))
                list.add(pi);
        };
        //zuerst History, dann aktuell laufende damit von alt zu neu ist
        processesHistory.forEach(consumer);
        processes.forEach(consumer);
        return list;
    }
    
    public synchronized boolean clean()
    {
        boolean foundAny = false;
        
        for(Iterator<ProcessInfo> it = processes.iterator(); it.hasNext();)
        {
            ProcessInfo o = it.next();
            if(!o.shouldKeepOnList())
            {
                LOGGER.info("Removing {} {}", o.getService(), o.getName());
                foundAny = true;
                try{
                    o.updateExitCode();
                    store.saveProcessInfo(o);
                }catch(RuntimeException e){
                    LOGGER.error("Error updating process info", e);
                }
                
                it.remove();
                processesHistory.add(o);
            }
        }
        
        return foundAny;
    }

    public void killAll()
    {
        processes.parallelStream().forEach(p -> {
            p.destroyForcibly();
        });
    }

    public Map<String, Path> getProcessLogDirectories(String service) throws IOException
    {
        return store.getProcessLogDirectories(service);
    }
    
    public Path getProcessLogDirectory(String service, String task) throws IOException
    {
        return store.getProcessLogDirectory(service, task);
    }
    
    public synchronized void notifyProcessFinished()
    {
        if(!clean())
            LOGGER.warn("Didn't find any process that finished");
    }
}
