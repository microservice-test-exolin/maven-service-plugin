package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatedDeployment
{
    private Long id;
    private String environment;
    private String description;
    private String sha1;
    private String ref;

    public Long getId()
    {
        return id;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSha1()
    {
        return sha1;
    }

    public String getRef()
    {
        return ref;
    }
}
