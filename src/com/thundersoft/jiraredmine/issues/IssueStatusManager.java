package com.thundersoft.jiraredmine.issues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.thundersoft.jiraredmine.config.ContentConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class IssueStatusManager {

    final private List<IssueStatus> mRedmineStatusList = new ArrayList<IssueStatus>();

    final private HashMap<String, IssueStatus> mNameToIssueStatus =
            new HashMap<String, IssueStatus>();

    final private HashMap<Integer, IssueStatus> mIdToIssueStatus =
            new HashMap<Integer, IssueStatus>();

    final private HashMap<IssueStatus, ArrayList<String>> mRedmineJira =
            new HashMap<IssueStatus, ArrayList<String>>();

    final private HashMap<String, ArrayList<IssueStatus>> mJiraRedmine =
            new HashMap<String, ArrayList<IssueStatus>>();

    public IssueStatusManager(List<IssueStatus> list) {
        mRedmineStatusList.clear();
        mRedmineStatusList.addAll(list);
        initizlize();
    }

    public void reInitialize() {
        mNameToIssueStatus.clear();
        mIdToIssueStatus.clear();
        mRedmineJira.clear();
        mJiraRedmine.clear();
        initizlize();
    }

    private void initizlize() {
        ContentConfig config = SystemConfig.getInstance().getContentConfig();
        for (IssueStatus status : mRedmineStatusList) {
            mNameToIssueStatus.put(status.getName(), status);
            mIdToIssueStatus.put(status.getId(), status);

            String values = config.getProperty(ContentConfig.STATUS_CONFIG_KEY + status.getName());
            if (values == null) {
                Log.error(getClass(), "There is not config " + ContentConfig.STATUS_CONFIG_KEY + status.getName());
//                throw new RuntimeException(
//                        "There is not config " + ContentConfig.STATUS_CONFIG_KEY + status.getName());
                continue;
            }

            String[] statues = values.split(",");
            ArrayList<String> jiraStatuses = mRedmineJira.get(status);
            if (jiraStatuses == null) {
                jiraStatuses = new ArrayList<String>();
            }
            for (String jira : statues) {
                String jiraStat = jira.trim();
                if (jiraStat != null && !jiraStat.isEmpty()) {
                    ArrayList<IssueStatus> redmineStatues = mJiraRedmine.get(jiraStat);
                    if (redmineStatues == null) {
                        redmineStatues = new ArrayList<IssueStatus>();
                        mJiraRedmine.put(jiraStat, redmineStatues);
                    }
                    redmineStatues.add(status);
                    jiraStatuses.add(jiraStat);
                }
            }
            if (!jiraStatuses.isEmpty()) {
                mRedmineJira.put(status, jiraStatuses);
            }
        }
    }

    public IssueStatus getRedmineIssueStatusByIssue(Issue redmine) {
        return mIdToIssueStatus.get(redmine.getStatusId());
    }

    public List<IssueStatus> getRedmineIssueStatus(JiraIssue jira) {
        return mJiraRedmine.get(jira.getStatus());
    }

    public IssueStatus getBetterRedmineIssueStatus(JiraIssue jira) {
        List<IssueStatus> list = getRedmineIssueStatus(jira);
        IssueStatus status = list.get(0);
        if ("Verified".equalsIgnoreCase(jira.getStatus()) && jira.isRejected()) {
            for (IssueStatus stat : list) {
                if ("Rejected".equals(stat.getName())) {
                    status = stat;
                    break;
                }
            }
        }
        return status;
    }

    public List<String> getJiraStatus(Issue redmine) {
        IssueStatus status = getRedmineIssueStatusByIssue(redmine);
        return mRedmineJira.get(status);
    }

    public int getClosedStatusId() {
        IssueStatus status = mNameToIssueStatus.get("Closed");
        return (status != null ? status.getId() : -1);
    }
}
