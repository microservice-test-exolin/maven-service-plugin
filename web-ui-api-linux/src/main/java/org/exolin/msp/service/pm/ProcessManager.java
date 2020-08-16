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
        this.processesHistory = store.getProcessInfos();
    }

    public synchronized ProcessInfo register(String service, String name, List<String> cmd, Path workingDirectory, String title, long startTime, String initiator)
    {
        clean();
        ProcessInfo pi = new ProcessInfo(service, name, startTime, workingDirectory.toAbsolutePath().normalize(), cmd, title, initiator, null);
        processes.add(pi);
        store.save(pi);
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
        processes.forEach(consumer);
        processesHistory.forEach(consumer);
        return list;
    }
    
    private void clean()
    {
        for (Iterator<ProcessInfo> it = processes.iterator(); it.hasNext();)
        {
            ProcessInfo o = it.next();
            if(!o.shouldKeepOnList())
            {
                o.updateExitCode();
                store.save(o);
                it.remove();
                processesHistory.add(o);
            }
        }
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
    
    public synchronized void notifyProcessFinished()
    {
        clean();
    }
}
