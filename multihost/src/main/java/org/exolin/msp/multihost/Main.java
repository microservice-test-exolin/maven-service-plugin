package org.exolin.msp.multihost;

import ch.qos.logback.classic.LoggerContext;
import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.exolin.msp.testservice.SecondMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author tomgk
 */
@SpringBootApplication
public class Main
{
    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws IOException, IOException
    {
        try{
            System.setProperty("spring.profiles.active", "dev,debug");
            
            //disable initialisation by spring to prevent multiple inizializations
            //System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");

            MDC.put("module", "root");
            ConfigurableApplicationContext parent = new SpringApplication(SecondMain.class).run(args);
            
            run(parent, "621-bot", Main.class, args);
            
            for(int i=1;i<=10;++i)
                run(parent, "test-app-"+i, SecondMain.class, args);
            
            Thread.sleep(1000);
            
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            lc.getLoggerList().stream().flatMap(l -> toStream(l.iteratorForAppenders())).distinct().forEach(System.out::println);
            
            //System.exit(0);
        }catch(Throwable e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static <T> Stream<T> toStream(Iterator<T> it)
    {
        return StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
          false);
    }
    
    private static final ThreadLocal MODULE_NAME = new InheritableThreadLocal(){
        @Override
        protected Object childValue(Object parentValue)
        {
            MDC.put("module", (String)parentValue);
            return parentValue;
        }
    };
    
    private static void run(ConfigurableApplicationContext parent, String name, Class<?> clazz, String[] args)
    {
        MDC.put("module", name);
        
        /*
        SpringApplication run = new SpringApplication(clazz);
        run.setBannerMode(Banner.Mode.LOG);
        run.setWebApplicationType(WebApplicationType.NONE);
        */
        
        SpringApplicationBuilder run = new SpringApplicationBuilder(clazz)
                .bannerMode(Banner.Mode.LOG)
                .web(WebApplicationType.NONE)
                //.main(clazz)
                //.properties(props)
                ;
        
        if(parent != null)
            run.parent(parent);
        
        //SpringApplication run = b.application();

        ThreadGroup threadGroup = new ThreadGroup(name);
        Thread t = new Thread(threadGroup, () -> {
            MDC.put("module", name);
            MODULE_NAME.set(name);
            //b.run(args);
            run.run(args);
        }, name+".Main");
        t.start();
    }
}
