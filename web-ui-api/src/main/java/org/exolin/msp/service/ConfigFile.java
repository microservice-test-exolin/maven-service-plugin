package org.exolin.msp.service;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author tomgk
 */
public interface ConfigFile
{
    Map<String, String> get();
    void set(String key, String value);
    void save() throws IOException;
}
