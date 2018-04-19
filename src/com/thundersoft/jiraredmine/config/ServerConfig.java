package com.thundersoft.jiraredmine.config;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

public class ServerConfig extends Properties {
    /**
     * 
     */
    private static final long serialVersionUID = -6043597342075128776L;

    public static final String REDMINE_CONFIG_KEY = "redmine.config.";
    public static final String JIRA_CONFIG_KEY = "jira.config.";

    public static final String URL_KEY = "url";
    public static final String USER_KEY = "user";
    public static final String PASSWD_KEY = "passwd";
    public static final String PROJECT = "project";
    public static final String JQL_URL = "jqlurl";
    public static final String JQL_VAL = "jqlval";
    public static final String ISSUE_DATA = "issuedata";

    public String getRedmineUrl() {
        return (String) get(REDMINE_CONFIG_KEY + URL_KEY);
    }

    public String getRedmineLoginUser() {
        return (String) get(REDMINE_CONFIG_KEY + USER_KEY);
    }

    public String getRedmineLoginPasswd() {
        return (String) get(REDMINE_CONFIG_KEY + PASSWD_KEY);
    }

    public String getRedmineProject() {
        return (String) get(REDMINE_CONFIG_KEY + PROJECT);
    }

    public String getJiraLoginUser() {
        return (String) get(JIRA_CONFIG_KEY + USER_KEY);
    }

    public String getJiraLoginPasswd() {
        return (String) get(JIRA_CONFIG_KEY + PASSWD_KEY);
    }

    public String getJiraBrowserUrl(String jiraKey) {
        String url = (String) get(JIRA_CONFIG_KEY + URL_KEY);
        return url + jiraKey;
    }

    public String getJiraDataUrl(String jiraKey) {
        String url = (String) get(JIRA_CONFIG_KEY + ISSUE_DATA);
        return url + jiraKey;
    }

    public String getJiraJqlUrl() {
        String url = (String) get(JIRA_CONFIG_KEY + JQL_URL);
        String jql = (String) get(JIRA_CONFIG_KEY + JQL_VAL);
        return url + URLEncoder.encode(jql);
    }
}
