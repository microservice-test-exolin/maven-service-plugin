package org.exolin.msp.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.exolin.msp.core.PseudoAbstraction;
import org.ini4j.Ini;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeployMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable 
        {
        }

        @Override
        protected void after()
        {
        }
    };
    
    private File pom;
    private Path simDir;
    
    @Before
    public void setup() throws IOException
    {
        pom = new File("../test-service/pom.xml").getCanonicalFile();
        simDir = pom.toPath().resolveSibling("target/simulator");
        
        Files.createDirectories(simDir.resolve("etc/systemd/system"));
    }
    
    @After
    public void tearDown() throws IOException
    {
        if(Files.exists(simDir))
        {
            try{
                deleteRecursivly(simDir);
            }catch(IOException|RuntimeException e){
                e.printStackTrace();
                throw e;
            }
        }
    }
    
    private void deleteRecursivly(Path path) throws IOException
    {
        if(Files.isDirectory(path))
        {
            System.err.println("Reading directory "+path);
            try(DirectoryStream<Path> files = Files.newDirectoryStream(path))
            {
                for(Path p: files)
                    deleteRecursivly(p);
            }
        }
        
        System.err.println("Deleting "+path);
        try{
            Files.delete(path);
        }catch(DirectoryNotEmptyException e){
            throw new IOException(path+" contains "+Files.walk(path)
                    .map(d -> d.relativize(path).toString())
                    .collect(Collectors.joining(",")), e);
        }catch(AccessDeniedException e){
            
        }
    }
    
    @Test
    public void testCleanDeploy() throws Exception
    {
        testDeploy0();
    }
    
    @Test
    public void testReDeployWithOldJar() throws Exception
    {
        Path pseudoOldJar = simDir.resolve("home/exolin/services/test-service/bin/xy.jar");
        Files.createDirectories(pseudoOldJar.getParent());
        assertExists(pseudoOldJar.getParent());
        Files.newOutputStream(pseudoOldJar).close();
        
        testDeploy0();
    }
    
    private void testDeploy0() throws Exception
    {
        assertTrue(pom.exists());
        
        DeployMojo deploy = (DeployMojo)rule.lookupConfiguredMojo(pom.getParentFile(), "deploy");
        assertNotNull(deploy);
        deploy.execute(simDir, new PseudoAbstraction(new LogAdapter(deploy.getLog())));
        
        assertExists(simDir.resolve("home/exolin/services/test-service"));
        assertExists(simDir.resolve("home/exolin/services/test-service/start.sh"));
        assertEquals(new HashSet<>(Arrays.asList(
                "test-service-1.0-SNAPSHOT.jar",
                "slf4j-api-1.7.25.jar",
                "log4j-over-slf4j-1.7.25.jar"
        )), list(simDir.resolve("home/exolin/services/test-service/bin")));
        
        Path serviceFile = simDir.resolve("etc/systemd/system/test-service.service");
        assertExists(serviceFile);
        
        org.ini4j.Ini a = new Ini(serviceFile.toFile());
        assertEquals("/bin/bash /home/exolin/services/test-service/start.sh", a.get("Service", "ExecStart"));
        assertEquals("exolin", a.get("Service", "User"));
        assertEquals("always", a.get("Service", "Restart"));
        assertEquals("1", a.get("Service", "RestartSec"));
        assertEquals("simple", a.get("Service", "Type"));
        
        List<String> x = Files.readAllLines(simDir.resolve("home/exolin/services/test-service/start.sh"));
        String first = x.get(0);
        x.remove(0);
        assertTrue(first.startsWith("#Generated by "+Constants.ID+" at "));
        
        assertEquals(String.join("\n", Arrays.asList(
                "set -e",
                "NAME=test-service",
                "DIR=/home/exolin/services/$NAME",
                "cd $DIR/bin",
                "echo Starting >> $DIR/log/service.out.log",
                "/usr/bin/java "
                        + "-Dsystem.baseDirectory=$DIR "
                        + "-Dsystem.logDirectory=$DIR/log "
                        + "-Dsystem.configDirectory=$DIR/cfg"
                        + " -jar $DIR/bin/test-service-1.0-SNAPSHOT.jar >> $DIR/log/service.out.log 2>&1",
                "echo Stopped >> $DIR/log/service.out.log"
        )), String.join("\n", x));
        
        assertEquals("jar", rule.getVariableValueFromObject(deploy, "packaging"));
    }
    
    private void assertExists(Path path)
    {
        assertTrue(path.toAbsolutePath().toString()+" not existing", Files.exists(path));
    }
    
    private void assertNotExists(Path path)
    {
        assertFalse(path.toAbsolutePath().toString()+" exists", Files.exists(path));
    }
    
    private Set<String> list(Path dir) throws IOException
    {
        try(Stream<Path> i = Files.list(dir))
        {
            return i.map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        }
    }
    
    @Test
    public void testDeployWithMissingServiceDir() throws Exception
    {
        Files.delete(simDir.resolve("etc/systemd/system"));
        
        DeployMojo deploy = (DeployMojo)rule.lookupConfiguredMojo(pom.getParentFile(), "deploy");
        assertNotNull(deploy);
        try{
            deploy.execute(simDir, new PseudoAbstraction(new LogAdapter(deploy.getLog())));
        }catch(MojoExecutionException e){
            assertNotNull(e.getCause());
            assertEquals(NoSuchFileException.class, e.getCause().getClass());
            assertEquals("missing etc/systemd/system", ((NoSuchFileException)e.getCause()).getReason());
        }
    }
}
