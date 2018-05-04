package com.thundersoft.jiraredmine;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

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

//        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
//
//            @Override
//            public URLStreamHandler createURLStreamHandler(String protocol) {
//                if ("file".equals(protocol)) {
//                    return new sun.net.www.protocol.file.Handler();
//                }
//                return null;
//            }
//            
//        });
        Log.info(Main.class, "================= Issue Syncronize =====================");

        SystemConfig system = SystemConfig.getInstance();
        ServerConfig config = system.getServerConfig();

        RedmineManager redmine = RedmineManagerFactory.createWithUserAuth(
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
            public void onCompare(Issue redmine, JiraIssue jira) {
                try {
                    synchronizer.synchronize(redmine, jira);
                } catch (RedmineException e) {
                    Log.error(getClass(),
                            "Synchronizing jira [" + jira + "] and redmine [" + redmine + "]", e);
                }
            }

            @Override
            public void onCompareCompleted() {
            }
        });
        Log.info(Main.class, "=========== Issue Syncronize completed =====================");
    }

}
