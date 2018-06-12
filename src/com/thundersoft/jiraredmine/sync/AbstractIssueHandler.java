package com.thundersoft.jiraredmine.sync;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taskadapter.redmineapi.CustomFieldManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;
import com.thundersoft.jiraredmine.accounts.AccountsManager;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.issues.IssuePriorityManager;
import com.thundersoft.jiraredmine.issues.IssueStatusManager;
import com.thundersoft.jiraredmine.logger.Log;

public abstract class AbstractIssueHandler implements IIssueHandler {

    final protected RedmineManager mRedmineMgr;
    protected AccountsManager mAccountMgr;
    protected IssueStatusManager mStatusMgr;
    protected IssuePriorityManager mPriorityMgr;
    protected CustomFieldManager mCustomFieldMgr;
    protected Map<String, Version> mVersionsMap;

    public AbstractIssueHandler(RedmineManager redmine) {
        mRedmineMgr = redmine;
        mAccountMgr = new AccountsManager(redmine.getUserManager());
        try {
            mStatusMgr = new IssueStatusManager(redmine.getIssueManager().getStatuses());
            mPriorityMgr = new IssuePriorityManager(redmine.getIssueManager().getIssuePriorities());

            ProjectManager projectMgr = redmine.getProjectManager();
            Project project = projectMgr.getProjectByKey(
                    SystemConfig.getInstance().getServerConfig().getRedmineProject());
            List<Version> versions = projectMgr.getVersions(project.getId());

            mVersionsMap = new ConcurrentHashMap<String, Version>();
            for (Version version : versions) {
                mVersionsMap.put(version.getName(), version);
            }
        } catch (RedmineException e) {
            Log.error(getClass(), "", e);
        }
        mCustomFieldMgr = redmine.getCustomFieldManager();
    }

    protected Version getVersionByName(String name) {
        return mVersionsMap.get(name);
    }
}
