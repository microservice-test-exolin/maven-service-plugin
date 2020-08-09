package org.exolin.msp.service.sa;

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
    void restart(String name) throws IOException;
    void getStatus(String name) throws IOException;
}
