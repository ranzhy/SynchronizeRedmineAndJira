package com.thundersoft.jiraredmine.sync;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.thundersoft.jiraredmine.issues.JiraIssue;

public class RedmineSynchronizer {

    final private IssueManager mIssueMgr;
    final private IIssueHandler mCallback; 

    public RedmineSynchronizer(IssueManager issueMgr, IIssueHandler callback) {
        mIssueMgr = issueMgr;
        mCallback = callback;
    }

    public void synchronize(Issue redmine, JiraIssue jira) throws RedmineException {
        if (redmine == null) {
            onRedmineIssueMissed(jira);
        } else if (jira == null || ! jira.isValidIssue()) {
            mCallback.onJiraIssueMissed(redmine);
        } else if (mCallback.checkIssueMatch(redmine, jira)){
            onUpdateRedmineIssue(redmine, jira);
        } else {
            onRedmineIssueMissed(jira);
            mCallback.onJiraIssueMissed(redmine);
        }
    }

    private void onRedmineIssueMissed(JiraIssue jira) throws RedmineException {
        Issue redmine = mCallback.onRedmineIssueMissed(jira);
        if (redmine != null) {
            redmine = mIssueMgr.createIssue(redmine);
            System.out.println("Added " + redmine + " for " + jira);
        }
    }

    private void onUpdateRedmineIssue(Issue redmine, JiraIssue jira) throws RedmineException {
        if (mCallback.onSynchronize(redmine, jira)) {
            mIssueMgr.update(redmine);
        }
    }
}
