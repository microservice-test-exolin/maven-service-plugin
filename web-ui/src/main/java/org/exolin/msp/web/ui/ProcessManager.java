package org.exolin.msp.web.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author tomgk
 */
public class ProcessManager
{
    public static class ProcessInfo
    {
        private final Process process;
        private final List<String> cmd;
        private final String title;
        private final long startTime;

        public ProcessInfo(Process process, List<String> cmd, String title, long startTime)
        {
            this.process = process;
            this.cmd = cmd;
            this.title = title;
            this.startTime = startTime;
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
            return System.currentTimeMillis() - startTime;
        }
    }
    
    private final List<ProcessInfo> processes = new ArrayList<>();
    
    public synchronized void register(Process process, List<String> cmd, String title, long startTime)
    {
        clean();
        processes.add(new ProcessInfo(process, cmd, title, startTime));
    }

    public List<ProcessInfo> getProcesses()
    {
        return processes;
    }
    
    private void clean()
    {
        processes.removeIf(p -> !p.getProcess().isAlive());
    }

    void killAll()
    {
        processes.parallelStream().forEach(p -> {
            p.getProcess().destroyForcibly();
        });
    }
}
