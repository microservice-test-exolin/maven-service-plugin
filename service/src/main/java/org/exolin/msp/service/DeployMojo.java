package org.exolin.msp.service;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.PACKAGE)//PROCESS_SOURCES )
public class DeployMojo extends BaseMojo
{
    @Override
    public void execute() throws MojoExecutionException
    {
        URL resource = DeployMojo.class.getResource("/install-base.sh");
        if(resource == null)
            throw new MojoExecutionException("Script not found");
        
        serviceFile = new File("target", "service");
        startSh = new File("target", "start.sh");
        
        try{
            getLog().info("Create service file");
            createServiceFile(serviceFile);
            
            getLog().info("Create start script");
            createStartSh(startSh);
            
            deploy(new PseudoAbstraction(getLog()), serviceName, jar.toPath(), serviceUser, startSh.toPath());
        }catch(IOException ex){
            throw new MojoExecutionException("Couldn't deploy", ex);
        }
    }
    
    private void deploy(SystemAbstraction sys, String name, Path jar, String user, Path startScript) throws IOException
    {
        if(!Files.exists(jar))
            throw new NoSuchFileException("No jar: "+jar);
        
        Path simDir = Paths.get("target/simulator");
        Files.createDirectories(simDir.resolve("etc/systemd/system"));
        
        Path localServiceFile = serviceFile.toPath();
        Path serviceDestFile = simDir.resolve("etc/systemd/system/"+name+".service");
        Path serviceDir = simDir.resolve("home/"+user+"/services/"+name);
        Path serviceBinDir = serviceDir.resolve("bin");
        Path serviceLogDir = serviceDir.resolve("log");
        Path jarDest = serviceBinDir.resolve(name+".jar");

        getLog().info("Stopping service...");
        //service $NAME stop || echo Service was not running

        //Setup directories
        createDirectories(serviceBinDir);
        createDirectories(serviceLogDir);
        //system("sudo", "chown", "-R", user, serviceDir.toFile().getAbsolutePath());
        sys.setOwner(serviceDir, serviceUser);
        
        //Copy JAR and dependencies
        copy(jar, jarDest);
        copyDirectoryContent(Paths.get("target", "lib"), serviceBinDir);
        copy(startScript, serviceBinDir.resolve(startScript.getFileName()));
        getLog().info("Copied jar file to "+jarDest);

        //Install service file
        copy(localServiceFile, serviceDestFile);
        getLog().info("Copied service file to "+serviceDestFile);
        sys.reloadDeamon();

        getLog().info("Installed");

        sys.restart(name);
        sys.getStatus(name);
    }

    private void _copy(Path src, Path dest) throws IOException
    {
        try{
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
    }

    private void copy(Path src, Path dest) throws IOException
    {
        try{
            getLog().info("Copy "+src+" to "+dest);
            _copy(src, dest);
        }catch(IOException e){
            throw new IOException("Failed to copy "+src+" to "+dest+": "+e, e);
        }
    }
    
    private void copyDirectoryContent(Path src, Path dest) throws IOException
    {
        getLog().info("Copy "+src+"/* to "+dest);
        
        try(DirectoryStream<Path> dir = Files.newDirectoryStream(src))
        {
            for(Path f: dir)
            {
                getLog().info("  Copy "+src.relativize(f));
                
                _copy(f, dest.resolve(f.getFileName()));
            }
        }
    }

    private void createDirectories(Path dir) throws IOException
    {
        getLog().info("Create directory "+dir);
        Files.createDirectories(dir);
    }
}
