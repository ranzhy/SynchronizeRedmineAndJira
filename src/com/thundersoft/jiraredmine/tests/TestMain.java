package com.thundersoft.jiraredmine.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.text.html.parser.DocumentParser;

//import com.atlassian.jira.rest.client.api.JiraRestClient;
//import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
//import com.atlassian.jira.rest.client.api.domain.Issue;
//import com.atlassian.jira.rest.client.api.domain.SearchResult;
//import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
//import com.atlassian.util.concurrent.Promise;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestMain {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, ExecutionException, RedmineException, SAXException, IOException {
//        URI url = new URI("https://spdojira.eww.panasonic.com");
//        JiraRestClientFactory facotry = new AsynchronousJiraRestClientFactory();
//        JiraRestClient client = facotry.createWithBasicHttpAuthentication(url, "ranzy0631.ts", "Apr4444444");
//        Promise<SearchResult> results = client.getSearchClient().searchJql(
//                "assignee in (\"ranzy0631.ts\", \"hutc0513.ts\", \"leiqing0704.ts\", \"zenglei0802.ts\")"
//                + " AND status in (Open, \"Open(Re-Open)\", \"Open(Re-Release)\", Reopened, \"In Progress\")"
//                + " ORDER BY priority");
//        
//        Iterator<Issue> issues = results.get().getIssues().iterator();
//        while (issues.hasNext()) {
//            Issue issue = issues.next();
//            System.out.println("" + issue);
//        }
        System.setProperty ("jsse.enableSNIExtension", "false");
        RedmineManager manager = RedmineManagerFactory.createWithUserAuth(
                "http://202.231.92.5:8100/redmine/", "ranzy0631", "ranzy0631@123");
//        ProjectManager pgm = manager.getProjectManager();
//        List<Project> projects = pgm.getProjects();
//        for (Project project : projects) {
//            System.out.println(project + "");
//        }
        Project project = manager.getProjectManager().getProjectByKey("vienna_us_jirabug");
        IssueManager issueManager = manager.getIssueManager();
        List<IssueStatus> statuses = issueManager.getStatuses();
        IssueStatus rejected = null;
        IssueStatus closed = null;
        for (IssueStatus status : statuses) {
            System.out.println(status.getName() + " : " + status);
            if ("Rejected".equals(status.getName())) {
                rejected = status;
            } else if ("Closed".equals(status.getName())) {
                closed = status;
            }
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("project_id", "vienna_us_jirabug");
        parameters.put("status_id", "" + closed.getId());
//        List<Issue> issues = issueManager.getIssues(project.getName(), null);
        ResultsWrapper<Issue> isues = issueManager.getIssues(parameters);
        for (Issue issue : /*issues*/ isues.getResults()) {
//            System.out.println(issue + " : " + issue.getCustomFieldByName("JiraUrl"));
//            if (checkJiraAndRedmine(issue)) {
//                issueManager.update(issue);
//            }
            if ("Rejected".equals(issue.getStatusName())) {
                System.err.println(issue.getStatusName() + " : " + issue);
            } else {
                System.out.println(issue.getStatusName() + " : " + issue);
            }
        }
    }

    private static boolean checkJiraAndRedmine(Issue issue) throws IOException, SAXException {
        String url = issue.getCustomFieldByName("JiraUrl").getValue();
        CustomField field = issue.getCustomFieldByName("JIRA-BUG");
        if (field.getValue() != null && !field.getValue().trim().isEmpty()) {
            System.out.println("Skipping " + url + " for " + issue);
            return false;
        }
        URL jira = new URL(url);
        URLConnection conn = jira.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);

        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print("os_username=ranzy0631.ts&os_password=May5555555");
        out.flush();

        conn.connect();

        InputStream in = conn.getInputStream();
        InputSource inputSource = new InputSource(in);

        DOMParser parser = new DOMParser();
        parser.parse(inputSource);

        Document doc = parser.getDocument();
        Element jira_key = doc.getElementById("key-val");
        String kei_val = jira_key.getTextContent().trim();

        String note = "Update JIRA-BUG to " + kei_val;
        System.out.println(note + " for " + issue);
        field.setValue(kei_val);
        issue.setNotes(note);
//        Element assignee_val = doc.getElementById("assignee-val");
//        NodeList assigners = assignee_val.getChildNodes();
//        String assignee = "";
//        String name = "";
//        for (int i = 0; i < assigners.getLength(); i++) {
//            Node node = assigners.item(i);
//            if (node instanceof Element) {
//                Element e = (Element) node;
//                assignee = e.getAttribute("rel");
//                if (assignee != null && !assignee.trim().isEmpty()) {
//                    name = e.getTextContent().trim();
//                    break;
//                }
//            }
//        }
//
//        Element status_e = doc.getElementById("status-val");
//        String status = status_e.getTextContent().trim();
//
//        System.out.println(kei_val + ": " + issue.getId() + "\t"
//                + name + "(" + assignee + ") : " + issue.getAssigneeName() + "\n"
//                + status + ":" + issue.getStatusName() + "\t"
//                + issue.getSubject() + " : " + url);
        return true;
    }
}
