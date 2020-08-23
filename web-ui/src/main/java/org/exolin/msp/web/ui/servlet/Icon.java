package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;

/**
 *
 * @author tomgk
 */
public class Icon
{
    public static final Icon SERVICE = new Icon("layers");
    public static final Icon PROCESS = new Icon("loader");
    public static final Icon LOG = new Icon("file-text");
    public static final Icon START = new Icon("play");
    public static final Icon STOP = new Icon("stop-circle");
    public static final Icon RESTART = new Icon("circle");
    public static final Icon COMPILE = new Icon("code");
    public static final Icon DEPLOY = new Icon("chevrons-right");
    public static final Icon CODE = new Icon("code");
    public static final Icon SERVER = new Icon("server");
    public static final Icon HOME = new Icon("home");
    public static final Icon INFO = new Icon("info");

    private final String name;

    public Icon(String name)
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
    
    static void script(PrintWriter out)
    {
        out.append("<script src=\"https://unpkg.com/feather-icons/dist/feather.min.js\"></script>");
        out.append("<script>feather.replace()</script>");
    }
}
