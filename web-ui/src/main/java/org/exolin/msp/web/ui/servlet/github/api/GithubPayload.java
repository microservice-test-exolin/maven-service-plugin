package org.exolin.msp.web.ui.servlet.github.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubPayload
{
    private GithubRepository repository;
    private List<GithubCommit> commits;

    public GithubRepository getRepository()
    {
        return repository;
    }

    public List<GithubCommit> getCommits()
    {
        return commits != null ? commits : Collections.emptyList();
    }
    
    public GithubCommit getLatest()
    {
        return commits != null ? commits.get(commits.size()-1) : null;  //TOOD: ist letztes neuestes?
    }
}
