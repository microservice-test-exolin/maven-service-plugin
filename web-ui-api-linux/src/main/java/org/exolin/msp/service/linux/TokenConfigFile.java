package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.exolin.msp.service.ConfigFile;

/**
 *
 * @author tomgk
 */
public class TokenConfigFile implements ConfigFile
{
    private static final String KEY = "token";
    
    private final Path path;
    private String value;

    public TokenConfigFile(Path path, String value)
    {
        this.path = path;
        this.value = value;
    }
    
    @Override
    public Map<String, String> get()
    {
        return Collections.singletonMap(KEY, value);
    }

    @Override
    public void set(String key, String value)
    {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        
        if(!key.equals(KEY))
            throw new IllegalArgumentException();
        
        this.value = value;
    }

    @Override
    public void save() throws IOException
    {
        Files.write(path, value.getBytes(StandardCharsets.UTF_8));
    }
}
