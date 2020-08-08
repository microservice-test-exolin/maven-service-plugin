package org.exolin.msp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.exolin.msp.service.sa.SystemAbstraction;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.exolin.msp.service.sa.LinuxAbstraction;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.PACKAGE)//PROCESS_SOURCES )
public class DeployMojo extends BaseMojo
{
    @Override
    public void execute() throws MojoExecutionException
    {
        execute(Paths.get("/"), new LinuxAbstraction(getLog()));
    }
    
    public void execute(Path simDir, SystemAbstraction sys) throws MojoExecutionException
    {
        Path destStartSh = ServiceInfo.getBaseDirectory(serviceUser, serviceName).resolve(ServiceInfo.START_SH);
        
        try{
            if(!libDir.exists())
                throw new NoSuchFileException(libDir.toString(), null, "lib directory not present");
            if(!jar.exists())
                throw new NoSuchFileException(jar.toString(), null, "JAR not found");
            
            File serviceFile = new File(pluginDir(), serviceName+".service");
            File startSh = new File(pluginDir(), "start.sh");

            getLog().info("Create service file");
            Generator.createServiceFile(serviceFile, serviceDescription, serviceUser, destStartSh);
            
            getLog().info("Create start script");
            Generator.createStartSh(startSh, serviceName, serviceUser, jar.getName(), useConfigDirectory);
            
            deploy(sys, simDir, serviceName, jar.toPath(), libDir.toPath(), serviceUser, startSh.toPath());
        
            setupServiceFile(sys, simDir, serviceName, serviceFile.toPath());
        }catch(IOException ex){
            throw new MojoExecutionException("Couldn't deploy", ex);
        }
    }
    
    /**
     * 
     * @param sys
     * @param name
     * @param jar
     * @param serviceUser User under which the service is executed
     * @param startScript Script used to start service
     * @throws IOException 
     */
    private void deploy(SystemAbstraction sys, Path simDir, String name, Path jar, Path libDir, String serviceUser, Path startScript) throws IOException
    {
        if(!Files.exists(jar))
            throw new NoSuchFileException(jar.toString(), null, "missing JAR");
        if(!Files.exists(simDir.resolve("etc/systemd/system")))
            throw new NoSuchFileException(jar.toString());
        
        Path serviceDir = simDir.resolve("home/"+serviceUser+"/services/"+name);
        Path serviceBinDir = serviceDir.resolve("bin");
        Path serviceCfgDir = serviceDir.resolve("cfg");
        Path serviceLogDir = serviceDir.resolve("log");
        Path jarDest = serviceBinDir.resolve(jar.getFileName());

        getLog().info("Stopping service...");
        //service $NAME stop || echo Service was not running

        //Setup directories
        FileUtils.createDirectories(getLog(), serviceBinDir);
        
        if(useConfigDirectory)
            FileUtils.createDirectories(getLog(), serviceCfgDir);
        
        FileUtils.createDirectories(getLog(), serviceLogDir);
        //system("sudo", "chown", "-R", user, serviceDir.toFile().getAbsolutePath());
        sys.setOwner(serviceDir, serviceUser);
        
        //Copy JAR and dependencies
        FileUtils.copy(getLog(), jar, jarDest);
        FileUtils.copyDirectoryContent(getLog(), libDir, serviceBinDir);
        FileUtils.copy(getLog(), startScript, serviceDir.resolve(ServiceInfo.START_SH));
        getLog().info("Copied jar file to "+jarDest);
    }
    
    private void setupServiceFile(SystemAbstraction sys, Path simDir, String name, Path localServiceFile) throws IOException
    {
        Path serviceDestFile = simDir.resolve("etc/systemd/system/"+name+".service");
        
        //Install service file
        FileUtils.copy(getLog(), localServiceFile, serviceDestFile);
        getLog().info("Copied service file to "+serviceDestFile);
        sys.reloadDeamon();

        getLog().info("Installed");

        sys.restart(name);
        sys.getStatus(name);
    }
}
