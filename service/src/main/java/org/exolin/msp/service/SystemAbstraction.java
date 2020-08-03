package org.exolin.msp.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author tomgk
 */
public interface SystemAbstraction
{
    void setOwner(Path serviceDir, String serviceUser) throws IOException;
    void reloadDeamon() throws IOException;
    void restart(String name) throws IOException;
    void getStatus(String name) throws IOException;
}
