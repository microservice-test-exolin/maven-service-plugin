package org.exolin.msp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author tomgk
 */
public abstract class BaseMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    //@Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    //private File outputDirectory;
    
    @Parameter( defaultValue = "${project.packaging}", property = "packaging", required = true )
    protected String packaging;
    
    @Parameter(defaultValue = "start.sh", property = "packaging", required = true )
    protected File startSh;
    
    @Parameter(defaultValue = "service", property = "packaging", required = true )
    protected File serviceFile;
    
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}", property = "jar", required = true )
    protected File jar;
    
    @Parameter(defaultValue = "${service.user}", property = "serviceUser", required = true )
    protected String serviceUser;
    
    @Parameter(defaultValue = "${project.artifactId}", property = "serviceName", required = true )
    protected String serviceName;
    
    @Parameter(defaultValue = "${project.description}", property = "serviceTitle", required = true )
    protected String serviceTitle;
    
    protected void createServiceFile(File serviceFile) throws IOException
    {
        PrintWriter w = new PrintWriter(serviceFile);

        w.println("[Unit]");
        w.println("Description="+serviceTitle);
        w.println("After=network.target");
        w.println("StartLimitIntervalSec=0");
        w.println("[Service]");
        w.println("Type=simple");
        w.println("Restart=always");
        w.println("RestartSec=1");
        w.println("User="+serviceUser);
        w.println("ExecStart=/bin/bash /home/exolin/services/"+serviceName+"/bin/start.sh");
        w.println();
        w.println("[Install]");
        w.println("WantedBy=multi-user.target");
        w.close();
        if(w.checkError())
            throw new IOException("Failed to write file "+serviceFile);
    }
    
    protected void createStartSh(File file) throws IOException
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

            w.write("/usr/bin/java -jar $DIR/bin/$NAME.jar > $DIR/log/$NAME.log 2> $DIR/log/$NAME.error.log");
            w.newLine();

            w.write("echo Started $NAME");
            w.newLine();
        }
    }
}
