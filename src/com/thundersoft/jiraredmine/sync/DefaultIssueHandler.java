package com.thundersoft.jiraredmine.sync;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import com.taskadapter.redmineapi.CustomFieldManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Tracker;
import com.thundersoft.jiraredmine.accounts.AccountsManager;
import com.thundersoft.jiraredmine.accounts.LocalGroup;
import com.thundersoft.jiraredmine.accounts.LocalUser;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.issues.IssuePriorityManager;
import com.thundersoft.jiraredmine.issues.IssueStatusManager;
import com.thundersoft.jiraredmine.issues.JiraIssue;
import com.thundersoft.jiraredmine.logger.Log;

public class DefaultIssueHandler extends AbstractIssueHandler {

    public DefaultIssueHandler(RedmineManager redmine) {
        super(redmine);
    }

    @Override
    public Issue onRedmineIssueMissed(JiraIssue jira) {
        Issue issue = new Issue();
        String data = convertUTF8String(jira.getSubject());
        issue.setSubject(data);
        try {
            Project project = mRedmineMgr.getProjectManager().getProjectByKey(
                    SystemConfig.getInstance().getServerConfig().getRedmineProject());
            issue.setProjectId(project.getId());
            issue.setProjectName(project.getName());

            List<Tracker> trackeres = mRedmineMgr.getIssueManager().getTrackers();
            for (Tracker tracker : trackeres) {
                if (tracker.getName().equals("JiraBug")) {
                    issue.setTracker(tracker);
                    break;
                }
            }
        } catch (RedmineException e) {
            Log.error(getClass(), "", e);
            return null;
        }

        IssueStatus status = mStatusMgr.getBetterRedmineIssueStatus(jira);
        issue.setStatusId(status.getId());
        issue.setStatusName(status.getName());

        IssuePriority priority = mPriorityMgr.getRedmineIssuePriority(jira);
        issue.setPriorityId(priority.getId());
        issue.setPriorityText(priority.getName());

        LocalUser user = mAccountMgr.getUserByJiraId(jira.getAssigner());
        if (user == null) {
            Log.error(getClass(), "[onRedmineIssueMissed] Skiping " + jira);
            return null;
        }
        LocalGroup group = user.getGroup();
        LocalUser leader = group.getLeader();
        issue.setAssigneeId(leader.getRedmineUserId());
        issue.setAssigneeName(leader.getUserName());

        if (!(addCustomField(issue, "JIRA-BUG", jira.getKey())
                && addCustomField(issue, "JiraUrl", jira.getBrowseUrl())
                && addCustomField(issue, "Group", group.getGroupName()))) {
            Log.error(getClass(), "Creating redmine issue failed: " + issue + " for " + jira);
            return null;
        }

        issue.setCreatedOn(new Date(System.currentTimeMillis()));

        addComment(issue, "Auto create " + issue + " for " + jira);
        return issue;
    }

    private boolean addCustomField(Issue issue, String name, String val) {
        try {
            for (CustomFieldDefinition define : mCustomFieldMgr.getCustomFieldDefinitions()) {
                if (define.getName().equals(name)) {
                    issue.addCustomField(CustomFieldFactory.create(define.getId(), name, val));
                    return true;
                }
            }
        } catch (RedmineException e) {
            Log.error(getClass(), "", e);
        }
        return false;
    }

    @Override
    public boolean checkIssueMatch(Issue redmine, JiraIssue jira) {
        String jiraUrlKey = SystemConfig.getInstance().getContentConfig().getRedmineJiraUrlKey();
        CustomField JiraUrlField = redmine.getCustomFieldByName(jiraUrlKey);
        if (JiraUrlField == null) {
            return false;
        }
        return jira.getBrowseUrl().equals(JiraUrlField.getValue());
    }

    @Override
    public boolean onSynchronize(Issue redmine, JiraIssue jira) {
        boolean ret = false;
        ret |= syncSubject(redmine, jira);
        ret |= syncStatus(redmine, jira);
        ret |= syncAssignerAndGroup(redmine, jira);
        ret |= syncPriority(redmine, jira);
        // TODO more info
        return ret;
    }

    protected void addComment(Issue redmine, String comment) {
        Log.info(getClass(), comment + " for " + redmine);
        String old = redmine.getNotes();
        if (old != null && !old.trim().isEmpty()) {
            comment = old + "\n" + comment;
        }
        redmine.setNotes(comment);
    }

    protected String convertUTF8String(String data) {
        byte[] bytes = data.getBytes();
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return data;
    }

    protected boolean syncSubject(Issue redmine, JiraIssue jira) {
        String jiraSub = jira.getSubject();
        String redmineSub = redmine.getSubject();

        String data = convertUTF8String(jiraSub);
        if (!redmineSub.equals(data)) {
            addComment(redmine, "Auto changing subject: " + redmineSub + " --> " + data);
            redmine.setSubject(data);
        }
        return !redmineSub.equals(data);
    }

    protected boolean syncStatus(Issue redmine, JiraIssue jira) {
        List<IssueStatus> list = mStatusMgr.getRedmineIssueStatus(jira);
        if (list.contains(mStatusMgr.getRedmineIssueStatusByIssue(redmine))) {
            return false;
        }

        IssueStatus better = mStatusMgr.getBetterRedmineIssueStatus(jira);
        String comment = "Auto changing issue status " + redmine.getStatusName() + " --> " + better.getName();
        redmine.setStatusId(better.getId());
        redmine.setStatusName(better.getName());
        addComment(redmine, comment);
        return true;
    }

    protected boolean syncAssignerAndGroup(Issue redmine, JiraIssue jira) {
        String jiraAssigner = jira.getAssigner();
        String jiraMail = jiraAssigner;

        LocalUser jiraUser = mAccountMgr.getUserByJiraId(jiraMail);
        if (jiraUser == null && !jiraMail.endsWith(".ts")) {
            jiraUser = mAccountMgr.getGroupByName("PMC").getLeader();
        }

        LocalUser redmineUser = mAccountMgr.getUserByRedmineUserId(redmine.getAssigneeId());
        if (jiraUser == null || redmineUser == null) {
            Log.error(getClass(), "User incorrect: " + jiraUser
                    + "(" + jiraMail + ") : " + redmineUser + "(" + redmine.getAssigneeName() + ")");
            return false;
        }

        if (jiraUser.getGroupName().equals(redmineUser.getGroupName())) {
            // Same group, skiping
            return false;
        }

        boolean ret = false;
        if ("PMC".equals(jiraUser.getGroupName())) {
            ret = updateGroupAndAssigner(redmine, jiraUser, true);
        } else {
            ret = updateGroupAndAssigner(redmine, jiraUser, false);
        }
        return ret;
    }

    private boolean updateGroupAndAssigner(Issue issue, LocalUser jira, boolean focusLeader) {
        LocalUser assigner = jira;
        if (focusLeader) {
            assigner = jira.getGroup().getLeader();
        }

        String comment = "Auto assigne issue " + issue.getAssigneeName() + " --> " + assigner.getFullName();

        issue.setAssigneeId(assigner.getRedmineUserId());
        issue.setAssigneeName(assigner.getFullName());

        CustomField groupField = issue.getCustomFieldByName("Group");
        groupField.setValue(jira.getGroupName());
        addComment(issue, comment);
        return true;
    }

    protected boolean syncPriority(Issue redmine, JiraIssue jira) {
        IssuePriority jPriority = mPriorityMgr.getRedmineIssuePriority(jira);
        IssuePriority rPriority = mPriorityMgr.getRedmineIssuePriorityByIssue(redmine);

        if (!jPriority.equals(rPriority)) {
            String comment = "Auto change priority " + rPriority + " --> " + jPriority;
            redmine.setPriorityId(jPriority.getId());
            redmine.setPriorityText(jPriority.getName());
            addComment(redmine, comment);
        }
        return !jPriority.equals(rPriority);
    }

    @Override
    public void onJiraIssueMissed(Issue redmine) {
        Log.info(getClass(), "Missed " + redmine.getStatusName() + " jira issue for " + redmine);
    }

}
