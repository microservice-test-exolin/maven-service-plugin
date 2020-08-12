package org.exolin.msp.web.ui;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.exolin.msp.core.PseudoAbstraction;
import org.exolin.msp.core.SystemAbstraction;
import static org.exolin.msp.web.ui.Main.run;
import org.exolin.msp.web.ui.pm.ProcessManager;
import org.exolin.msp.web.ui.stub.StubService;
import org.exolin.msp.web.ui.stub.StubServices;

/**
 *
 * @author tomgk
 */
public class LocalMain
{
    public static void main(String[] args) throws Exception
    {
        Files.write(Paths.get("test.log"), Arrays.asList("Log Entry"));
        
        ProcessManager pm = new ProcessManager();
        
        SystemAbstraction sys = new PseudoAbstraction(new LogAdapter(PseudoAbstraction.class));
        Services services = new StubServices(Arrays.asList(
                    new StubService("test-mittens-discord", sys),
                    new StubService("test-milkboi-discord", sys),
                    new StubService("test-milkboi-telegram", sys)
            ));
        
        run(pm, sys, services);
    }
}
