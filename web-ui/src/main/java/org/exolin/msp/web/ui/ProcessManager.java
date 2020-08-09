package org.exolin.msp.web.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author tomgk
 */
public class ProcessManager
{
    public static class ProcessInfo
    {
        private final String service;
        private final Process process;
        private final List<String> cmd;
        private final String title;
        private final long startTime;

        public ProcessInfo(String service, Process process, List<String> cmd, String title, long startTime)
        {
            this.service = service;
            this.process = process;
            this.cmd = cmd;
            this.title = title;
            this.startTime = startTime;
        }

        public String getService()
        {
            return service;
        }

        public Process getProcess()
        {
            return process;
        }

        public List<String> getCmd()
        {
            return cmd;
        }

        public String getTitle()
        {
            return title;
        }
        
        public long getRuntime()
        {
            if(!process.isAlive())
                return -1;
            
            return System.currentTimeMillis() - startTime;
        }
    }
    
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

    void killAll()
    {
        processes.parallelStream().forEach(p -> {
            p.getProcess().destroyForcibly();
        });
    }
}
