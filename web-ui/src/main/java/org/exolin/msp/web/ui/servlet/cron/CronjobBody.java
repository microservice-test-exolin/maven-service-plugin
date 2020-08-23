package org.exolin.msp.web.ui.servlet.cron;

import java.io.Writer;

/**
 *
 * @author tomgk
 */
public interface CronjobBody
{
    void execute(Writer out) throws Exception;
}
