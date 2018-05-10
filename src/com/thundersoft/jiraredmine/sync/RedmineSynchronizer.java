package com.thundersoft.jiraredmine.sync;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.thundersoft.jiraredmine.issues.JiraIssue;
import com.thundersoft.jiraredmine.logger.Log;

public class RedmineSynchronizer {

    final private IssueManager mIssueMgr;
    final private IIssueHandler mCallback; 

    public RedmineSynchronizer(IssueManager issueMgr, IIssueHandler callback) {
        mIssueMgr = issueMgr;
        mCallback = callback;
    }

    public void synchronize(Issue redmine, JiraIssue jira, boolean clearRedmine) throws RedmineException {
        if (redmine == null) {
            onRedmineIssueMissed(jira);
        } else if (jira == null || ! jira.isValidIssue()) {
            onJiraIssueMissed(redmine, clearRedmine);
        } else if (mCallback.checkIssueMatch(redmine, jira)){
            onUpdateRedmineIssue(redmine, jira);
        } else {
            Log.info(getClass(), "redmine " + redmine + " not match with " + jira);
            onRedmineIssueMissed(jira);
            onJiraIssueMissed(redmine, clearRedmine);
        }
    }

    private void onRedmineIssueMissed(JiraIssue jira) throws RedmineException {
        Issue redmine = mCallback.onRedmineIssueMissed(jira);
        if (redmine != null) {
            redmine = mIssueMgr.createIssue(redmine);
            Log.info(getClass(), "Added " + redmine + " for " + jira);
        }
    }

    private void onUpdateRedmineIssue(Issue redmine, JiraIssue jira) throws RedmineException {
        if (mCallback.onSynchronize(redmine, jira)) {
            mIssueMgr.update(redmine);
        }
    }

    private void onJiraIssueMissed(Issue redmine, boolean clearRedmine) throws RedmineException {
        if (mCallback.onJiraIssueMissed(redmine) && clearRedmine) {
            Log.error(getClass(), "Try to deleted " + redmine);
            mIssueMgr.deleteIssue(redmine.getId());
        }
    }
}
