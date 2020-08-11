package org.exolin.msp.web.ui;

import org.exolin.msp.core.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomgk
 */
public class LogAdapter implements Log
{
    private final Logger logger;

    public LogAdapter(Class<?> clazz)
    {
        this(LoggerFactory.getLogger(clazz));
    }

    public LogAdapter(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void warn(String string)
    {
        logger.warn(string);
    }

    @Override
    public void info(String string)
    {
        logger.info(string);
    }
}
