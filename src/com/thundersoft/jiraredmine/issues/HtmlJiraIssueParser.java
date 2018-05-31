package com.thundersoft.jiraredmine.issues;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Override
    public String getReporter() {
        Element reporter_val = doc.getElementById("reporter-val");
        NodeList reporters = reporter_val.getChildNodes();
        String reporter = "";
        for (int i = 0; i < reporters.getLength(); i++) {
            Node node = reporters.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                reporter = e.getAttribute("rel");
                if (reporter != null && !reporter.trim().isEmpty()) {
                    return reporter.trim();
                }
            }
        }
        return "";
    }

    @Override
    public String getComponent() {
        Element reporter_val = doc.getElementById("components-field");
        NodeList component = reporter_val.getChildNodes();
        String components = "";
        for (int i = 0; component != null && i < component.getLength(); i++) {
            Node node = component.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                if ("a".equals(e.getNodeName())) {
                    components += ", " + e.getTextContent().trim();
                }
            }
        }
        return components.substring(1);
    }

    @Override
    public Date getUpdatedTime() {
        Element updated_date = doc.getElementById("updated-date");
        NodeList date = updated_date.getChildNodes();
        String time = "";
        for (int i = 0; i < date.getLength(); i++) {
            Node node = date.item(i);
            if (node instanceof Element) {
                Element datetime = (Element) node;
                time = datetime.getAttribute("datetime");
                if (time != null && !time.trim().isEmpty()) {
                    time = time.trim().replace("T", " ");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mmz");
                    try {
                        return formatter.parse(time);
                    } catch (ParseException e) {
                        Log.error(getClass(),"[HtmlJiraIssueParser]getUpdatedTime ", e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getDetectionPhase() {
        Element detectionPhase_val = doc.getElementById("customfield_10004-val");
        String detectionPhase = detectionPhase_val.getTextContent();
        detectionPhase = (detectionPhase != null ? detectionPhase.trim() : "");
        return detectionPhase;
    }

}
