package org.exolin.msp.web.ui.servlet;

import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import org.eclipse.jetty.server.Utf8HttpWriter;

/**
 *
 * @author tomgk
 */
public class Feather
{
    public static final Feather SERVICE= new Feather("layers");
    public static final Feather PROCESS= new Feather("loader");
    public static final Feather LOG= new Feather("file-text");
    public static final Feather START= new Feather("play");
    public static final Feather STOP= new Feather("stop-circle");
    public static final Feather RESTART= new Feather("circle");
    public static final Feather COMPILE= new Feather("code");
    public static final Feather DEPLOY= new Feather("chevrons-right");
    public static final Feather CODE= new Feather("code");
    public static final Feather SERVER = new Feather("server");
    public static final Feather HOME = new Feather("home");
    public static final Feather INFO = new Feather("info");
    
    private final String name;

    public Feather(String name)
    {
        this.name = name;
    }
    
    public void writeTo(Appendable out) throws IOException
    {
        out.append("<span data-feather=\""+name+"\"></span> ");
    }
    
    public void writeTo(PrintWriter out)
    {
        out.append("<span data-feather=\""+name+"\"></span> ");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        try{
            writeTo(sb);
        }catch(IOException e){
            throw new UncheckedIOException("Unexpected I/O error", e);
        }
        return sb.toString();
    }
}
