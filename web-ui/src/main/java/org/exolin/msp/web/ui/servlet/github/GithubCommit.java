package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit
{
    private String id;
    private String message;
    private String timestamp;
    private String url;
    private String sha1;
    
    private List<String> added;
    private List<String> removed;
    private List<String> modified;

    public String getSha1()
    {
        return sha1;
    }
}
