package org.exolin.msp.service.linux.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.exolin.msp.service.ConfigFile;

/**
 *
 * @author tomgk
 */
public class PropertiesConfigFile implements ConfigFile
{
    private final Path path;
    private final Properties properties;

    public PropertiesConfigFile(Path path, Properties properties)
    {
        this.path = path;
        this.properties = properties;
    }

    @Override
    public Map<String, String> get()
    {
        return (Map)Collections.unmodifiableMap(properties);
    }

    @Override
    public void set(String key, String value)
    {
        properties.setProperty(key, value);
    }

    @Override
    public void save() throws IOException
    {
        try(BufferedWriter out = Files.newBufferedWriter(path))
        {
            properties.store(out, null);
        }
    }
}
