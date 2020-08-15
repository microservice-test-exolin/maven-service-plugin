package org.exolin.msp.service.pm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class ProcessManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);
    private final ProcessStore store;
    private final List<ProcessInfo> processes = new ArrayList<>();
    private final List<ProcessInfo> processesHistory = new ArrayList<>();

    public ProcessManager(ProcessStore store)
    {
        this.store = store;
    }

    public synchronized void register(String service, String name, Process process, List<String> cmd, String title, long startTime)
    {
        clean();
        ProcessInfo pi = new ProcessInfo(service, name, startTime, process, cmd, title);
        processes.add(pi);
        store.add(pi);
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
    
    private void clean()
    {
        for (Iterator<ProcessInfo> it = processes.iterator(); it.hasNext();)
        {
            ProcessInfo o = it.next();
            if(!o.getProcess().isAlive())
            {
                it.remove();
                processesHistory.add(o);
            }
        }
    }

    public void killAll()
    {
        processes.parallelStream().forEach(p -> {
            p.getProcess().destroyForcibly();
        });
    }
}
