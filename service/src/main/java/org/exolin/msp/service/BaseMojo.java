package org.exolin.msp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author tomgk
 */
public abstract class BaseMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project.packaging}", property = "packaging", required = true )
    protected String packaging;
    
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}", property = "jar", required = true )
    protected File jar;
    
    @Parameter(defaultValue = "${project.build.directory}/lib", property = "jar", required = true )
    protected File libDir;
    
    @Parameter(defaultValue = "${project.build.directory}/service-deployment", property = "service-deployment-temp", required = true )
    protected File pluginDir;
    
    @Parameter(defaultValue = "${service.user}", property = "serviceUser", required = true )
    protected String serviceUser;
    
    @Parameter(defaultValue = "${project.artifactId}", property = "serviceName", required = true)
    protected String serviceName;
    
    @Parameter(defaultValue = "${project.description}", property = "serviceDescription", required = true )
    protected String serviceDescription;
    
    protected File pluginDir() throws IOException
    {
        if(!pluginDir.exists())
        {
            if(!pluginDir.mkdir())
                throw new IOException("Couldn't create "+pluginDir);
        }

        return pluginDir;
    }
    
    protected static void createServiceFile(File serviceFile, String serviceTitle, String serviceUser, Path startSh) throws IOException
    {
        PrintWriter w = new PrintWriter(serviceFile);

        w.println("[Unit]");
        w.println("Description="+serviceTitle);
        w.println("After=network.target");
        w.println("StartLimitIntervalSec=0");
        w.println();
        w.println("[Service]");
        w.println("Type=simple");
        w.println("Restart=always");
        w.println("RestartSec=1");
        w.println("User="+serviceUser);
        w.println("ExecStart=/bin/bash "+startSh.toFile());
        w.println();
        w.println("[Install]");
        w.println("WantedBy=multi-user.target");
        w.close();
        if(w.checkError())
            throw new IOException("Failed to write file "+serviceFile);
    }
    
    private static void writeSysProp(Writer w, String name, String value) throws IOException
    {
        //safety check
        if(name.contains(" ") || value.contains(" "))
            throw new IllegalArgumentException();
        
        w.append(" -D").append(name).append("=").append(value);
    }
    
    protected static void createStartSh(File file, String serviceName, String serviceUser) throws IOException
    {
        try(FileWriter o = new FileWriter(file); BufferedWriter w = new BufferedWriter(o))
        {
            w.write("set -e");
            w.newLine();

            w.write("NAME="+serviceName);
            w.newLine();

            w.write("DIR=/home/"+serviceUser+"/services/$NAME");
            w.newLine();

            w.write("cd $DIR/bin");
            w.newLine();

            w.write("/usr/bin/java");
            writeSysProp(w, "system.baseDirectory", "$DIR");
            w.write(" -jar ");
            /**/w.write("$DIR/bin/$NAME.jar");
            w.write(" >> $DIR/log/$NAME.log ");
            w.write("2>> $DIR/log/$NAME.error.log");
            w.newLine();

            w.write("echo Started $NAME");
            w.newLine();
        }
    }
}
