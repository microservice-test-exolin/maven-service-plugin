package org.exolin.msp.service;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.PACKAGE)//PROCESS_SOURCES )
public class VerifyMojo extends BaseMojo
{
    @Override
    public void execute() throws MojoExecutionException
    {
        //if(packaging.equals("pom"))
        //    return;
        if(!startSh.exists())
            throw new MojoExecutionException("Missing "+startSh);
        
        if(!serviceFile.exists())
            throw new MojoExecutionException("Missing "+serviceFile);
        
        getLog().info("All okay");
    }
}
