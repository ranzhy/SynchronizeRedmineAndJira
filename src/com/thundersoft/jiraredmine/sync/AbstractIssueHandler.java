package com.thundersoft.jiraredmine.sync;

import com.taskadapter.redmineapi.CustomFieldManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.thundersoft.jiraredmine.accounts.AccountsManager;
import com.thundersoft.jiraredmine.issues.IssuePriorityManager;
import com.thundersoft.jiraredmine.issues.IssueStatusManager;

public abstract class AbstractIssueHandler implements IIssueHandler {

    final protected RedmineManager mRedmineMgr;
    protected AccountsManager mAccountMgr;
    protected IssueStatusManager mStatusMgr;
    protected IssuePriorityManager mPriorityMgr;
    protected CustomFieldManager mCustomFieldMgr;

    public AbstractIssueHandler(RedmineManager redmine) {
        mRedmineMgr = redmine;
        mAccountMgr = new AccountsManager(redmine.getUserManager());
        try {
            mStatusMgr = new IssueStatusManager(redmine.getIssueManager().getStatuses());
            mPriorityMgr = new IssuePriorityManager(redmine.getIssueManager().getIssuePriorities());
        } catch (RedmineException e) {
            e.printStackTrace();
        }
        mCustomFieldMgr = redmine.getCustomFieldManager();
    }
}
