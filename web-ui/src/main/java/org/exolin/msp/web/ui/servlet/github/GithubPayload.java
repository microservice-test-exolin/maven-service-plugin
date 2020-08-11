package org.exolin.msp.web.ui.servlet.github;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author tomgk
 */
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
}
