package org.exolin.msp.service;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.exolin.msp.service.sa.PseudoAbstraction;
import org.ini4j.Ini;
import org.junit.After;
import org.junit.Before;

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
    public void testSomething() throws Exception
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
        
        assertEquals(Arrays.asList(
                "set -e",
                "NAME=test-service",
                "DIR=/home/exolin/services/$NAME",
                "cd $DIR/bin",
                "/usr/bin/java -Dsystem.baseDirectory=$DIR -jar $DIR/bin/test-service-1.0-SNAPSHOT.jar >> $DIR/log/$NAME.log 2>> $DIR/log/$NAME.error.log",
                "echo Started $NAME"
        ), Files.readAllLines(simDir.resolve("home/exolin/services/test-service/start.sh")));
        
        assertEquals("jar", rule.getVariableValueFromObject(deploy, "packaging"));
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
