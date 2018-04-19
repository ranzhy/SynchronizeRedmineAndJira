package com.thundersoft.jiraredmine.issues;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;

public class LocalIssueManager {

    public static interface IssueCompareCallback {
        public void onCompare(Issue redmine, JiraIssue jira);
        public void onCompareCompleted();
    }

    private Map<String, Issue> mRedmineIssues = new HashMap<String, Issue>(0);
    private Set<String> mJireKeys = new HashSet<String>(0);
    // private Map<String, JiraIssue> mJireIssues = new HashMap<String, JiraIssue>();

    public void loadAllRedmineIssue(IssueManager issueMgr) throws RedmineException {
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        List<Issue> issues = issueMgr.getIssues(config.getRedmineProject(), null);
        mRedmineIssues = new HashMap<String, Issue>(issues.size());
        for (Issue issue : issues) {
            String key = SystemConfig.getInstance().getContentConfig().getRedmineJiraBugkey();
            CustomField field = issue.getCustomFieldByName(key);
            key = field.getValue();
            mRedmineIssues.put(key, issue);
        }
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
                mJireKeys.add(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void compareIssues(IssueCompareCallback callback) throws IOException, SAXException {
        Set<String> keies = new HashSet<String>(mJireKeys);
        keies.addAll(mRedmineIssues.keySet());
        for (String bugKey : keies) {
            mJireKeys.remove(bugKey);
            Issue issue = mRedmineIssues.remove(bugKey);
            JiraIssue jira = new JiraIssue(bugKey);
            if (issue == null && (!jira.isValidIssue() ||
                    !jira.getAssigner().endsWith(".ts"))) {
                // Skip the issue, it's not relate with Thundersoft
                System.out.println("Skipping " + jira);
                continue;
            }
            callback.onCompare(issue, jira);
        }
        callback.onCompareCompleted();
    }
}
