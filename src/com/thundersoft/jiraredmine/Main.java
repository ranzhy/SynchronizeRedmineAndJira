package com.thundersoft.jiraredmine;

import java.io.IOException;

import org.xml.sax.SAXException;

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

    public static void main(String[] args) throws RedmineException {
        System.setProperty ("jsse.enableSNIExtension", "false");

        SystemConfig system = SystemConfig.getInstance();
        ServerConfig config = system.getServerConfig();

        RedmineManager redmine = RedmineManagerFactory.createWithUserAuth(
                config.getRedmineUrl(), config.getRedmineLoginUser(), config.getRedmineLoginPasswd());
        AbstractIssueHandler issueHandler = system.createIssueHandler(redmine);
        RedmineSynchronizer synchronizer = new RedmineSynchronizer(redmine.getIssueManager(), issueHandler);
        LocalIssueManager issugMgr = new LocalIssueManager();
        issugMgr.loadAllRedmineIssue(redmine.getIssueManager());
        issugMgr.loadAllJiraIssue();
        issugMgr.compareIssues(new IssueCompareCallback() {

            @Override
            public void onCompare(Issue redmine, JiraIssue jira) {
                try {
                    synchronizer.synchronize(redmine, jira);
                } catch (RedmineException e) {
                    Log.error(getClass(), "", e);
                }
            }

            @Override
            public void onCompareCompleted() {
            }
        });
        Log.info(Main.class, "=========== Issue Syncronize completed =====================");
    }

}
