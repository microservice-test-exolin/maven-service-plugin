package org.exolin.msp.service.pm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author tomgk
 */
public class ProcessManager
{
    private final List<ProcessInfo> processes = new ArrayList<>();
    private final List<ProcessInfo> processesHistory = new ArrayList<>();
    
    public synchronized void register(String service, Process process, List<String> cmd, String title, long startTime)
    {
        clean();
        processes.add(new ProcessInfo(service, process, cmd, title, startTime));
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
