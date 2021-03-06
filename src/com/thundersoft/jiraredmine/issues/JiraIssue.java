package com.thundersoft.jiraredmine.issues;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class JiraIssue {

    private String mBrowseUrl;
    private String mDataUrl;
    private String mKey;

    private IJiraIssueParser mParser;

    private String mAssigner = "";

    protected JiraIssue(String bugKey){
        mKey = bugKey;
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        mDataUrl = config.getJiraDataUrl(bugKey);
        mBrowseUrl = config.getJiraBrowserUrl(bugKey);
        initialize();
    }

    public void reInitialized() {
        initialize();
    }

    private void initialize() {
        if (!initialize(mDataUrl, new JsonJiraIssueParser())) {
            Log.error(getClass(), "Rest API failed for " + mDataUrl + ", try " + mBrowseUrl);
            initialize(mBrowseUrl, new HtmlJiraIssueParser());
        }
    }

    private boolean initialize(String url, IJiraIssueParser parser) {
        String data = JiraClient.getInstance().loadData(url);
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
        if (parser.initialize(in)) {
            mParser = parser;
        }
        return mParser != null;
    }

    public boolean isValidIssue() {
        return mParser != null;
    }

    public String getBrowseUrl() {
        return mBrowseUrl;
    }

    public String getDataUrl() {
        return mDataUrl;
    }

    public String getKey() {
        return mKey;
    }

    public String getStatus() {
        return mParser == null ? null : mParser.getStatus();
    }

    public String getRejects() {
        return mParser == null ? null : mParser.getRejects();
    }

    public boolean isRejected() {
        String reject = getRejects();
        return reject != null && !reject.isEmpty() &&
                !"NONE".equalsIgnoreCase(reject);
    }

    public String getAssigner() {
        String assigner = mAssigner;
        if (assigner == null || mAssigner.isEmpty()) {
            assigner = mParser == null ? null : mParser.getAssigner();
            if (mParser != null && (assigner == null || assigner.isEmpty())) {
                assigner = mParser.getReporter();
            }
            mAssigner = assigner;
        }
        return assigner;
    }

    public String getPriority() {
        return mParser == null ? null : mParser.getPriority();
    }

    public String getSubject() {
        return mParser == null ? null : convertUTF8String(mParser.getSubject());
    }

    protected String convertUTF8String(String data) {
        byte[] bytes = data.getBytes();
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return data;
    }

    public String getComponent() {
        return mParser == null ? null : mParser.getComponent();
    }

    public Date getUpdatedTime() {
        return mParser == null ? null : mParser.getUpdatedTime();
    }

    public String getDetectionPhase() {
        return mParser == null ? null : mParser.getDetectionPhase();
    }

    @Override
    public String toString() {
        return "["+ (isValidIssue() ? "" : "(Invalid)") + getKey() + "(" + getAssigner() + "):" + getSubject() + "]";
    }
}
