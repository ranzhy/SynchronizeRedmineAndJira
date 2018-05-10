package com.thundersoft.jiraredmine.issues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class LocalIssueManager {

    public static interface IssueCompareCallback {
        public void onCompare(Issue redmine, JiraIssue jira, boolean clearRedmine);
        public void onCompareCompleted();
    }

    private Map<String, ArrayList<Issue>> mRedmineIssues = new HashMap<String, ArrayList<Issue>>(0);
    private Map<String, ArrayList<Issue>> mRedmineClosedIssues = new HashMap<String, ArrayList<Issue>>(0);
    private Set<String> mJireKeys = new HashSet<String>(0);
    // private Map<String, JiraIssue> mJireIssues = new HashMap<String, JiraIssue>();

    public void loadAllRedmineIssue(IssueManager issueMgr) {
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        
        try {
            // open issues
            List<Issue> issues = issueMgr.getIssues(config.getRedmineProject(), null);
            mRedmineIssues = loadRedmineIssuesToMap(issues, null);

            // closed issues
            HashMap<String, String> queryParameters = new HashMap<String, String>();
            queryParameters.put("project_id", config.getRedmineProject());
            queryParameters.put("status_id", "" + getClosedStatusId(issueMgr));
            ResultsWrapper<Issue> result = issueMgr.getIssues(queryParameters);
            mRedmineClosedIssues = loadRedmineIssuesToMap(result.getResults(), null);
        } catch (RedmineException e) {
            throw new RuntimeException("Load redmine issue failed, Abort", e);
        }
    }

    private int getClosedStatusId(IssueManager issueMgr) throws RedmineException {
        List<IssueStatus> statuses = issueMgr.getStatuses();
        IssueStatus closed = null;
        for (IssueStatus status : statuses) {
            if ("Closed".equals(status.getName())) {
                closed = status;
            }
        }
        return closed.getId();
    }

    private Map<String, ArrayList<Issue>> loadRedmineIssuesToMap(
            List<Issue> issues, Map<String, ArrayList<Issue>> redmines) {
        if (redmines == null) {
            redmines = new HashMap<String, ArrayList<Issue>>(issues.size());
        }

        for (Issue issue : issues) {
            String key = SystemConfig.getInstance().getContentConfig().getRedmineJiraBugkey();
            CustomField field = issue.getCustomFieldByName(key);
            key = field.getValue().trim();
            ArrayList<Issue> issueList = redmines.get(key);
            if (issueList == null) {
                issueList = new ArrayList<Issue>(1);
            }
            issueList.add(issue);
            redmines.put(key, issueList);
        }

        return redmines;
    }

    public void loadAllJiraIssue() {
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        String url = config.getJiraJqlUrl();
        doLoadJiraKey(url);
    }

    private void doLoadJiraKey(String url) {
        String data = JiraClient.getInstance().loadData(url);
        try {
            JSONObject obj = new JSONObject(data);
            obj = obj.getJSONObject("issueTable");
            int total = obj.getInt("total");
            JSONArray array = obj.getJSONArray("issueKeys");
            mJireKeys = new HashSet<String>(total);
            for (int i = 0; i < total; i++) {
                String key = array.getString(i);
                mJireKeys.add(key.trim());
            }
        } catch (JSONException e) {
            Log.error(getClass(), "", e);
            throw new RuntimeException("Load jira issue failed, Abort", e);
        }
    }

    public void compareIssues(IssueCompareCallback callback) {
        Set<String> keies = new HashSet<String>(mJireKeys);
        keies.addAll(mRedmineIssues.keySet());
        for (String bugKey : keies) {
            bugKey = bugKey.trim();
            mJireKeys.remove(bugKey);
            ArrayList<Issue> issueList = mRedmineIssues.remove(bugKey);
            if (issueList == null) {
                issueList = mRedmineClosedIssues.remove(bugKey);
            }
            JiraIssue jira = new JiraIssue(bugKey);
            if (issueList == null && (!jira.isValidIssue() ||
                    !jira.getAssigner().endsWith(".ts"))) {
                // Skip the issue, it's not relate with Thundersoft
                Log.debug(getClass(), "Never tracing the issue: " + jira);
                continue;
            }
            Issue issue = null;
            if (issueList != null) {
                issue = issueList.remove(0);
            }
            callback.onCompare(issue, jira, false);

            if (issueList != null && !issueList.isEmpty()) {
                for (Issue redmine : issueList) {
                    callback.onCompare(redmine, null, true);
                }
            }
        }
        callback.onCompareCompleted();
    }
}
