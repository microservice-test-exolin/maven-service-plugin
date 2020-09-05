package org.exolin.msp.service;

import java.io.File;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests für {@link Generator}
 *
 * @author tomgk
 */
public class GeneratorTest
{
    @Test
    public void testValidateMaxHeapSize()
    {
        Generator.validateMaxHeapSize("5m");
        Generator.validateMaxHeapSize("50m");
        Generator.validateMaxHeapSize("500m");
        Generator.validateMaxHeapSize("1g");
        
        try{
            Generator.validateMaxHeapSize("1a");
            fail();
        }catch(IllegalArgumentException e){
        }
        
        try{
            Generator.validateMaxHeapSize("1");
            fail();
        }catch(IllegalArgumentException e){
        }
    }
}
