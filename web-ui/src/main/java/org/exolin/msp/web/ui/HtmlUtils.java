package org.exolin.msp.web.ui;

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
}
