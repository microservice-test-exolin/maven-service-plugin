package org.exolin.msp.service;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.exolin.msp.service.sa.PseudoAbstraction;
import static org.hamcrest.CoreMatchers.is;
import org.ini4j.Ini;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ErrorCollector;

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
            List<Path> files = Files.walk(simDir).collect(Collectors.toList());
            Collections.reverse(files);

            for(Path p: files)
                Files.delete(p);
        }
    }
    
    /**
     * @throws Exception if any
     */
    @Test
    public void testDeploy() throws Exception
    {
        assertTrue(pom.exists());
        
        DeployMojo deploy = (DeployMojo)rule.lookupConfiguredMojo(pom.getParentFile(), "deploy");
        assertNotNull(deploy);
        deploy.execute(simDir, new PseudoAbstraction(deploy.getLog()));
        
        assertExists(simDir.resolve("home/exolin/services/test-service"));
        assertExists(simDir.resolve("home/exolin/services/test-service/start.sh"));
        assertExists(simDir.resolve("home/exolin/services/test-service/bin/test-service-1.0-SNAPSHOT.jar"));
        assertExists(simDir.resolve("home/exolin/services/test-service/bin/slf4j-api-1.7.25.jar"));
        assertExists(simDir.resolve("home/exolin/services/test-service/bin/log4j-over-slf4j-1.7.25.jar"));
        
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
        
        assertEquals(Arrays.asList(
                "set -e",
                "NAME=test-service",
                "DIR=/home/exolin/services/$NAME",
                "cd $DIR/bin",
                "/usr/bin/java "
                        + "-Dsystem.baseDirectory=$DIR "
                        + "-Dsystem.logDirectory=$DIR/log "
                        + "-Dsystem.configDirectory=$DIR/cfg"
                        + " -jar $DIR/bin/test-service-1.0-SNAPSHOT.jar >> $DIR/log/$NAME.log 2>> $DIR/log/$NAME.error.log",
                "echo Started $NAME"
        ), x);
        
        assertEquals("jar", rule.getVariableValueFromObject(deploy, "packaging"));
    }
    
    private void assertExists(Path path)
    {
        assertTrue(path.toAbsolutePath().toString()+" not existing", Files.exists(path));
    }
    
    @Rule
    public ErrorCollector collector;
    
    @Test
    public void testDeployWithMissingServiceDir() throws Exception
    {
        Files.delete(simDir.resolve("etc/systemd/system"));
        
        DeployMojo deploy = (DeployMojo)rule.lookupConfiguredMojo(pom.getParentFile(), "deploy");
        assertNotNull(deploy);
        try{
            deploy.execute(simDir, new PseudoAbstraction(deploy.getLog()));
        }catch(MojoExecutionException e){
            assertNotNull(e.getCause());
            collector.checkThat(e.getCause().getClass(), is((Object)NoSuchFileException.class));
            collector.checkThat(((NoSuchFileException)e.getCause()).getReason(), is("missing etc/systemd/system"));
        }
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
    {
        assertTrue(true);
    }
}
