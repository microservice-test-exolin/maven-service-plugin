package org.exolin.msp.web.ui;

import java.io.File;
import java.util.Collections;
import org.apache.maven.shared.invoker.*;

/**
 *
 * @author tomgk
 */
public class DeployUtility
{
    public void a() throws MavenInvocationException
    {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( "/path/to/pom.xml" ) );
        request.setGoals( Collections.singletonList( "install" ) );

        Invoker invoker = new DefaultInvoker();
        invoker.execute( request );
    }
}
