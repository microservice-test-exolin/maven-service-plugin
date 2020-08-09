package org.exolin.msp.core;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author tomgk
 */
public interface SystemAbstraction
{
    void setOwner(Path path, String user) throws IOException;
    void reloadDeamon() throws IOException;

    public void start(String name) throws IOException;
    public void stop(String name) throws IOException;
    public void restart(String name) throws IOException;
    public boolean isRunning(String name) throws IOException;
}
