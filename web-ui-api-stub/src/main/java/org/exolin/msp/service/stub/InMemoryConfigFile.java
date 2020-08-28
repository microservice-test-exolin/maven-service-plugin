package org.exolin.msp.service.stub;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.exolin.msp.service.ConfigFile;

/**
 *
 * @author tomgk
 */
public class InMemoryConfigFile implements ConfigFile
{
    private final Map<String, String> entries;

    public InMemoryConfigFile(Map<String, String> entries)
    {
        this.entries = entries;
    }

    @Override
    public Map<String, String> get()
    {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public void set(String key, String value)
    {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        entries.put(key, value);
    }

    @Override
    public void save() throws IOException
    {
        //nothing
    }
}
