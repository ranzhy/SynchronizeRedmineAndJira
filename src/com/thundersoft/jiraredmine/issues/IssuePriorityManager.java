package com.thundersoft.jiraredmine.issues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.thundersoft.jiraredmine.config.ContentConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;

public class IssuePriorityManager {

    final private List<IssuePriority> mRedminePriorityList = new ArrayList<IssuePriority>();

    final private HashMap<String, IssuePriority> mNameToIssuePriority =
            new HashMap<String, IssuePriority>();

    final private HashMap<Integer, IssuePriority> mIdToIssuePriority =
            new HashMap<Integer, IssuePriority>();

    final private HashMap<IssuePriority, String> mRedmineJira =
            new HashMap<IssuePriority, String>();

    final private HashMap<String, IssuePriority> mJiraRedmine =
            new HashMap<String, IssuePriority>();

    public IssuePriorityManager(List<IssuePriority> list) {
        mRedminePriorityList.clear();
        mRedminePriorityList.addAll(list);
        initizlize();
    }

    public void reInitialize() {
        mNameToIssuePriority.clear();
        mIdToIssuePriority.clear();
        mRedmineJira.clear();
        mJiraRedmine.clear();
        initizlize();
    }

    private void initizlize() {
        ContentConfig config = SystemConfig.getInstance().getContentConfig();
        for (IssuePriority priority : mRedminePriorityList) {
            mNameToIssuePriority.put(priority.getName(), priority);
            mIdToIssuePriority.put(priority.getId(), priority);

            String values = config.getProperty(ContentConfig.PRIORITY_CONFIG_KEY + priority.getName());
            if (values == null) {
                System.err.println("There is not config " + ContentConfig.PRIORITY_CONFIG_KEY + priority.getName());
                throw new RuntimeException(
                        "There is not config " + ContentConfig.PRIORITY_CONFIG_KEY + priority.getName());
            }

            String jiraPriority = values.trim();
            if (jiraPriority != null && !jiraPriority.isEmpty()) {
                mJiraRedmine.put(jiraPriority, priority);
                mRedmineJira.put(priority, jiraPriority);
            }
        }
    }

    public IssuePriority getRedmineIssuePriorityByIssue(Issue redmine) {
        return mIdToIssuePriority.get(redmine.getPriorityId());
    }

    public IssuePriority getRedmineIssuePriority(JiraIssue jira) {
        return mJiraRedmine.get(jira.getPriority());
    }

    public String getJiraPriority(Issue redmine) {
        IssuePriority priority = getRedmineIssuePriorityByIssue(redmine);
        return mRedmineJira.get(priority);
    }
}
