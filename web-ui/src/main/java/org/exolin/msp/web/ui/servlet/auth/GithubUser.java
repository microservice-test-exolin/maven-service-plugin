package org.exolin.msp.web.ui.servlet.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUser
{
    private String login;
    private String avatar_url;

    public String getLogin()
    {
        return login;
    }

    public String getAvatarUrl()
    {
        return avatar_url;
    }
}
