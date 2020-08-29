package org.exolin.msp.web.ui;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class Config
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    public static String KEY_GITHUB_CLIENT_ID = "github.client_id";
    public static String KEY_GITHUB_CLIENT_SECRET = "github.client_secret";
    public static String ALLOWED_USERS = "allowedUsers";
    public static String KEY_AUTH_TYPE = "auth.type";
    
    public enum AuthType
    {
        github, none
    }
    
    static String VALUE_AUTH_TYPE_GITHUB = "github";
    static String VALUE_AUTH_TYPE_NONE = "none";
    
    private final Properties properties;
    
    public static Config read(Path path) throws IOException
    {
        Properties properties = new Properties();
        try(Reader reader = Files.newBufferedReader(path))
        {
            properties.load(reader);
        }catch(NoSuchFileException e){
            LOGGER.warn("No config file {}, using empty configuration", path);
        }
        return new Config(properties);
    }

    public Config(Properties properties)
    {
        this.properties = properties;
    }
    
    <E extends Enum<E>> E get(String name, Class<E> enumClass)
    {
        String val = get(name);
        
        try{
            return Enum.valueOf(enumClass, val);
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException(name+" has invalid value "+val+", allowed are "+EnumSet.allOf(enumClass));
        }
    }
    
    String get(String name)
    {
        String v = properties.getProperty(name);
        
        if(v == null)
            throw new IllegalArgumentException("Missing "+name);
        
        return v;
    }

    Set<String> getStringSet(String name)
    {
        return new HashSet<>(Arrays.asList(get(name).split(",")));
    }
}
