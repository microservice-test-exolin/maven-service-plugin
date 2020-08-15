package org.exolin.msp.web.ui.servlet.auth;

/**
 *
 * @author tomgk
 */
public class GithubAuthException extends RuntimeException
{
    public GithubAuthException(String message)
    {
        super(message);
    }
}
