package org.exolin.msp.service;

import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author tomgk
 */
public class LogAdapter implements org.exolin.msp.core.Log
{
    private final Log log;

    public LogAdapter(Log log)
    {
        this.log = log;
    }

    @Override
    public void warn(String string)
    {
        log.warn(string);
    }

    @Override
    public void info(String string)
    {
        log.info(string);
    }
}
