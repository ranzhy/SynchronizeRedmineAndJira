package com.thundersoft.jiraredmine;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.issues.JiraIssue;
import com.thundersoft.jiraredmine.issues.LocalIssueManager;
import com.thundersoft.jiraredmine.issues.LocalIssueManager.IssueCompareCallback;
import com.thundersoft.jiraredmine.logger.Log;
import com.thundersoft.jiraredmine.sync.AbstractIssueHandler;
import com.thundersoft.jiraredmine.sync.RedmineSynchronizer;

public class Main {

    public static void main(String[] args) {
        System.setProperty ("jsse.enableSNIExtension", "false");

        Log.info(Main.class, "================= Issue Syncronize =====================");

        try {
            SystemConfig system = SystemConfig.getInstance();
            ServerConfig config = system.getServerConfig();

            final RedmineManager redmine = RedmineManagerFactory.createWithUserAuth(
                    config.getRedmineUrl(), config.getRedmineLoginUser(), config.getRedmineLoginPasswd());

            Log.info(Main.class, "Will create Issue handler for " + redmine);
            AbstractIssueHandler issueHandler = system.createIssueHandler(redmine);
            Log.info(Main.class, "Created Issue handler: " + issueHandler);

            RedmineSynchronizer synchronizer = new RedmineSynchronizer(redmine.getIssueManager(), issueHandler);
            LocalIssueManager issugMgr = new LocalIssueManager();

            Log.info(Main.class, "Will load and synchronize redmine issues and jira issues");
            issugMgr.loadAllRedmineIssue(redmine.getIssueManager());
            issugMgr.loadAllJiraIssue();
            issugMgr.compareIssues(new IssueCompareCallback() {

                @Override
                public void onCompare(Issue redmineIssue, JiraIssue jiraIssue, boolean clearRedmine) {
                    try {
                        if (redmineIssue != null) {
                            redmineIssue = redmine.getIssueManager().getIssueById(redmineIssue.getId());
                        }
                        synchronizer.synchronize(redmineIssue, jiraIssue, clearRedmine);
                    } catch (RedmineException e) {
                        Log.error(getClass(),
                                "Synchronizing jira [" + jiraIssue + "] and redmine [" + redmineIssue + "]", e);
                    }
                }

                @Override
                public void onCompareCompleted() {
                    Log.info(Main.class, "Issue Compare completed");
                }
            });
        } catch (Exception e) {
            Log.error(Main.class, "Syncronizing failed between JIRA and redmine in this time", e);
        } finally {
            Log.info(Main.class, "=========== Issue Syncronize completed =====================");
        }
    }

}
