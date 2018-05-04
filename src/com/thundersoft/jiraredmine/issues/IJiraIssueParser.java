package com.thundersoft.jiraredmine.issues;

import java.io.InputStream;

public interface IJiraIssueParser {

    public boolean initialize(InputStream in);

    public String getJiraKey();

    public String getStatus();

    public String getRejects();

    public String getAssigner();

    public String getSubject();

    public String getPriority();

    public String getReporter();
}
