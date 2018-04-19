package com.thundersoft.jiraredmine.sync;

import com.taskadapter.redmineapi.bean.Issue;
import com.thundersoft.jiraredmine.issues.JiraIssue;

public interface IIssueHandler {

    /**
     * Called when the issue has not been created in redmine.
     * @param jira
     * @return a need create issue; return null if creating is unnecessary.
     */
    public Issue onRedmineIssueMissed(JiraIssue jira);

    /**
     * Checking the redmine issue and jira are matched.
     * @param redmine
     * @param jira
     * @return true if match
     */
    public boolean checkIssueMatch(Issue redmine, JiraIssue jira);

    /**
     * Synchronizing the issue.
     * @param redmine [in/out] redmine issue, it may be update.
     * @param jira [readonly] Jira issue.
     * @return true, if redmine issue has been update; Or return false.
     */
    public boolean onSynchronize(Issue redmine, JiraIssue jira);

    /**
     * Called when the jira issue is missed.
     * @param redmine
     */
    public void onJiraIssueMissed(Issue redmine);
}
