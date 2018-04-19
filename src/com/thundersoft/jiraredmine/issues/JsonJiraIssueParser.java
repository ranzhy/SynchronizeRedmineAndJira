package com.thundersoft.jiraredmine.issues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonJiraIssueParser implements IJiraIssueParser {

    private JSONObject mJsonObj;
    private JSONObject mJsonFields;

    @Override
    public boolean initialize(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            mJsonObj = new JSONObject(buffer.toString());
            mJsonFields = mJsonObj.getJSONObject("fields");
        } catch (IOException | JSONException e) {
           System.err.println("[JsonJiraIssueParser] : " + e.getMessage());
        }
        return mJsonObj != null && mJsonFields != null;
    }

    private String getValue(JSONObject obj, String key) {
        try {
            return obj == null ? null : obj.getString(key);
        } catch (JSONException e) {
            System.err.println("[JsonJiraIssueParser]getValue("
                    + obj + ", " + key + ") : " + e.getMessage());
        }
        return null;
    }

    private JSONObject getChiledObject(JSONObject obj, String key) {
        try {
            return obj == null ? null : obj.getJSONObject(key);
        } catch (JSONException e) {
            System.err.println("[JsonJiraIssueParser]getChiledObject("
                    + obj + ", " + key + ") : " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getJiraKey() {
        return getValue(mJsonObj, "key");
    }

    @Override
    public String getStatus() {
        return getValue(getChiledObject(mJsonFields, "status"), "name");
    }

    @Override
    public String getRejects() {
        return getValue(getChiledObject(mJsonFields, "customfield_10101"), "value");
    }

    @Override
    public String getAssigner() {
        return getValue(getChiledObject(mJsonFields, "assignee"), "name");
    }

    @Override
    public String getSubject() {
        return getValue(mJsonFields, "summary");
    }

    @Override
    public String getPriority() {
        return getValue(getChiledObject(mJsonFields, "priority"), "name");
    }

}
