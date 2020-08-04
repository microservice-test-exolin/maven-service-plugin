package org.exolin.msp.service;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ini4j.Ini;

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

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething() throws Exception
    {
        File pom = new File("../test-service").getCanonicalFile();
        assertNotNull(pom);
        assertTrue(pom.exists());
        
        DeployMojo deploy = (DeployMojo)rule.lookupConfiguredMojo(pom, "deploy");
        assertNotNull(deploy);
        deploy.execute();
        
        Path simDir = pom.toPath().resolve("target/simulator");
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
        
        /*File outputDirectory = (File)rule.getVariableValueFromObject(deploy, "outputDirectory");
        assertNotNull(outputDirectory);
        assertTrue(outputDirectory.exists());

        File touch = new File(outputDirectory, "touch.txt");
        assertTrue(touch.exists());*/
    }
    
    private void assertExists(Path path)
    {
        assertTrue(path.toAbsolutePath().toString()+" not existing", Files.exists(path));
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
    {
        assertTrue(true);
    }
}
