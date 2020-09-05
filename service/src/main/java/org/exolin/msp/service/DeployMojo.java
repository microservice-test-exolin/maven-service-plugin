package org.exolin.msp.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.exolin.msp.core.LinuxAbstraction;
import org.exolin.msp.core.StatusType;
import org.exolin.msp.core.SystemAbstraction;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.PACKAGE)//PROCESS_SOURCES )
public class DeployMojo extends BaseMojo
{
    @Override
    public void execute() throws MojoExecutionException
    {
        execute(Paths.get("/"), new LinuxAbstraction(new LogAdapter(getLog())));
    }
    
    public void execute(Path simDir, SystemAbstraction sys) throws MojoExecutionException
    {
        FileUtils files = new FileUtils(getLog(), serviceUser, sys);
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
            Generator.createStartSh(startSh,
                    serviceName, serviceUser, maxHeapSize,
                    jar.getName(), useConfigDirectory);
            
            deploy(sys, files, simDir, serviceName, jar.toPath(), libDir.toPath(), serviceUser, startSh.toPath());
        
            setupService(sys, files, simDir, serviceName, serviceFile.toPath());
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
    private void deploy(SystemAbstraction sys, FileUtils files, Path simDir, String name, Path jar, Path libDir, String serviceUser, Path startScript) throws IOException
    {
        if(!Files.exists(jar))
            throw new NoSuchFileException(jar.toString(), null, "missing JAR");
        if(!Files.exists(simDir.resolve("etc/systemd/system")))
            throw new NoSuchFileException(simDir.resolve("etc/systemd/system").toString(), null, "missing etc/systemd/system");
        
        Path serviceDir = simDir.resolve("home/"+serviceUser+"/services/"+name);
        Path serviceBinDir = serviceDir.resolve("bin");
        Path serviceCfgDir = serviceDir.resolve("cfg");
        Path serviceLogDir = serviceDir.resolve("log");
        Path jarDest = serviceBinDir.resolve(jar.getFileName());

        getLog().info("Stopping service...");
        //service $NAME stop || echo Service was not running

        //Setup directories
        files.createDirectories(serviceBinDir);
        
        if(useConfigDirectory)
            files.createDirectories(serviceCfgDir);
        
        files.createDirectories(serviceLogDir);
        //system("sudo", "chown", "-R", user, serviceDir.toFile().getAbsolutePath());
        sys.setOwner(serviceDir, serviceUser);
        
        Path originalPath = serviceDir.resolve("original.path");
        Files.write(originalPath, jar.getParent().getParent().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8));
        sys.setOwner(originalPath, serviceUser);
        
        //Copy JAR and dependencies
        files.copy(jar, jarDest);
        files.replaceDirectoryContent(libDir, serviceBinDir, Collections.singleton(jarDest));
        files.copy(startScript, serviceDir.resolve(ServiceInfo.START_SH));
        getLog().info("Copied jar file to "+jarDest);
    }
    
    private void setupService(SystemAbstraction sys, FileUtils files, Path simDir, String name, Path localServiceFile) throws IOException
    {
        Path serviceDestFile = simDir.resolve("etc/systemd/system/"+name+".service");
        
        //Install service file
        files.copy(localServiceFile, serviceDestFile);
        getLog().info("Copied service file to "+serviceDestFile);
        sys.reloadDeamon();

        getLog().info("Installed");

        sys.restart(name);
        StatusType status = sys.getStatus(name).getStatus();
        if(status != StatusType.ACTIVE)
            throw new IllegalStateException(name+" not "+StatusType.ACTIVE+" "+name+" but "+status);
    }
}
