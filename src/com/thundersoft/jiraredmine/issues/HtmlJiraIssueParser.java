package com.thundersoft.jiraredmine.issues;

import java.io.IOException;
import java.io.InputStream;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thundersoft.jiraredmine.logger.Log;

public class HtmlJiraIssueParser implements IJiraIssueParser {

    Document doc = null;

    @Override
    public boolean initialize(InputStream in) {
        DOMParser parser = new DOMParser();
        InputSource inputSource = new InputSource(in);
        try {
            parser.parse(inputSource);
            doc = parser.getDocument();
        } catch (SAXException | IOException e) {
            Log.error(getClass(), "", e);
            return false;
        }
        return true;
    }

    @Override
    public String getJiraKey() {
        Element jira_key = doc.getElementById("key-val");
        return jira_key.getTextContent().trim();
    }

    @Override
    public String getStatus() {
        Element status_e = doc.getElementById("status-val");
        return status_e.getTextContent().trim();
    }

    @Override
    public String getRejects() {
        Element reject_e = doc.getElementById("customfield_10101-val");
        return reject_e.getTextContent().trim();
    }

    @Override
    public String getAssigner() {
        Element assignee_val = doc.getElementById("assignee-val");
        NodeList assigners = assignee_val.getChildNodes();
        String assignee = "";
        for (int i = 0; i < assigners.getLength(); i++) {
            Node node = assigners.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                assignee = e.getAttribute("rel");
                if (assignee != null && !assignee.trim().isEmpty()) {
                    return assignee.trim();
                }
            }
        }
        return "";
    }

    @Override
    public String getSubject() {
        Element reject_e = doc.getElementById("summary-val");
        return reject_e.getTextContent().trim();
    }

    @Override
    public String getPriority() {
        Element priority_e = doc.getElementById("priority-val");
        return priority_e.getTextContent().trim();
    }

}
