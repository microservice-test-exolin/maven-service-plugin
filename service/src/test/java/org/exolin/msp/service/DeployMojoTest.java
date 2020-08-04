package org.exolin.msp.service;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

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

        /*File outputDirectory = (File)rule.getVariableValueFromObject(deploy, "outputDirectory");
        assertNotNull(outputDirectory);
        assertTrue(outputDirectory.exists());

        File touch = new File(outputDirectory, "touch.txt");
        assertTrue(touch.exists());*/
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
    {
        assertTrue(true);
    }
}
