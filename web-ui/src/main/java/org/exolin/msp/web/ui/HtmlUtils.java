package org.exolin.msp.web.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.exolin.msp.web.ui.servlet.Icon;

/**
 *
 * @author tomgk
 */
public class HtmlUtils
{
    public static String escapeHTML(String string)
    {
        return string.replace("<", "&lt;").replace(">", "&gt;");
    }

    public static CharSequence cleanHTML(String string)
    {
        //incorrect
        return string.replace("<", "").replace(">", "");
    }
    
    public static void startInlineForm(PrintWriter out, String url)
    {
        out.append("<form action=\""+url+"\" method=\"POST\" style=\"display: inline\">");
    }

    public static void endInlineForm(PrintWriter out)
    {
        out.append("</form>");
    }
    
    public static void writeActionButton(Writer out, String action, Icon icon, String title) throws IOException
    {
        out.append("<button name=\"action\" value=\""+action+"\" class=\"btn btn-secondary btn-sm\">");
        if(icon != null)
            icon.writeTo(out);
        out.append(title).append("</button> ");
    }

    public static void writeHiddenInput(PrintWriter out, String name, String value)
    {
        out.append("<input type=\"hidden\" name=\"").append(name).append("\" value=\"").append(value).append("\">");
    }
}
