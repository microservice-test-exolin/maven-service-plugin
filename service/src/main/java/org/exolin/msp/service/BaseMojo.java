package org.exolin.msp.service;

import java.io.File;
import java.io.IOException;
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
    
    @Parameter(defaultValue = "${project.build.directory}", required = true )
    protected File targetDir;
    
    @Parameter(defaultValue = "${project.build.directory}/lib", property = "libDir", required = true )
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
}
