package com.thundersoft.jiraredmine.config;

import java.util.Properties;

public class ContentConfig extends Properties {
    /**
     * 
     */
    private static final long serialVersionUID = 6148074647393522876L;

    public static final String USERS_CONFIG_KEY = "users.config.";   // indicated by email prefix 
    public static final String GROUP_CONFIG_KEY = "group.config.";    //group leader mail

    public static final String STATUS_CONFIG_KEY = "status.config.";
    public static final String PRIORITY_CONFIG_KEY = "priority.config.";

    public String getRedmineJiraUrlKey() {
        return "JiraUrl";
    }

    public String getRedmineJiraBugkey() {
        return "JIRA-BUG";
    }
}
