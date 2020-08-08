package org.exolin.msp.system.lib;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author tomgk
 */
public class DBConfig
{
    private static Path file()
    {
        return Config.getConfigDirectory().resolve("database.connection");
    }
    
    private static Properties loadDBConfig() throws IOException
    {
        Path configFile = file();
        
        Properties properties = new Properties();
        try(Reader in = Files.newBufferedReader(configFile))
        {
            properties.load(in);
        }
        
        return properties;
    }
    
    public static Connection loadConnection() throws IOException, SQLException, ClassNotFoundException
    {
        Properties config;
        
        try{
            config = loadDBConfig();
        }catch(IOException e){
            throw new SQLException("Failed to load configuration from "+file(), e);
        }
        
        String url = config.getProperty("url");
        String driverClass = config.getProperty("driverClass");
        if(url == null)
            throw new SQLException("No url in "+file());
        if(driverClass != null)
        {
            try{
                Class.forName(driverClass);
            }catch(ClassNotFoundException e){
                throw new SQLException("Can't load "+driverClass, e);
            }
        }
        
        return DriverManager.getConnection(url, config);
    }
}
