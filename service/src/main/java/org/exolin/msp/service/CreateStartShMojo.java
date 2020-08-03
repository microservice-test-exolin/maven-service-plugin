package org.exolin.msp.service;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "create-start-sh", defaultPhase = LifecyclePhase.PACKAGE)//PROCESS_SOURCES )
public class CreateStartShMojo extends BaseMojo
{
    @Parameter(defaultValue = "start.sh", property = "startFile", required = true )
    protected File startFile;
    
    @Override
    public void execute() throws MojoExecutionException
    {
        try{
            createStartSh(startFile);
        }catch(IOException e){
            throw new MojoExecutionException("Failed to create file", e);
        }
    }
}
