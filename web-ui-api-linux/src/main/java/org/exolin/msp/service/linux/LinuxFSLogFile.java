package org.exolin.msp.service.linux;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.exolin.msp.service.LogFile;
import org.exolin.msp.web.ui.LognameGenerator;

/**
 *
 * @author tomgk
 */
public class LinuxFSLogFile extends LogFile
{
    private final Path path;

    public LinuxFSLogFile(String serviceName, Optional<String> processName, Path path)
    {
        super(serviceName, processName);
        this.path = path;
    }

    @Override
    public String getTitle()
    {
        return LognameGenerator.getLogFileTitle(getServiceName(), getProcessName(), getPath());
    }

    @Override
    public void writeTo(OutputStream out) throws IOException
    {
        Files.copy(path, out);
    }
    
    @Override
    public String getFileName()
    {
        return path.getFileName().toString();
    }

    public Path getPath()
    {
        return path;
    }
}
