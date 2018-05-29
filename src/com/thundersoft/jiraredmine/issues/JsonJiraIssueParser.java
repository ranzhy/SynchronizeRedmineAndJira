package com.thundersoft.jiraredmine.issues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thundersoft.jiraredmine.logger.Log;

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
           Log.error(getClass(), "", e);
        }
        return mJsonObj != null && mJsonFields != null;
    }

    private String getValue(JSONObject obj, String key) {
        try {
            return obj == null ? null : obj.getString(key);
        } catch (JSONException e) {
            Log.error(getClass(),"[JsonJiraIssueParser]getValue("
                    + obj + ", " + key + ")", e);
        }
        return null;
    }

    private JSONObject getChiledObject(JSONObject obj, String key) {
        try {
            return obj == null ? null : obj.getJSONObject(key);
        } catch (JSONException e) {
            Log.error(getClass(),"[JsonJiraIssueParser]getChiledObject("
                    + obj + ", " + key + ")", e);
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
        String assigner =  getValue(getChiledObject(mJsonFields, "assignee"), "name");
        return assigner == null ? "" : assigner;
    }

    @Override
    public String getSubject() {
        return getValue(mJsonFields, "summary");
    }

    @Override
    public String getPriority() {
        return getValue(getChiledObject(mJsonFields, "priority"), "name");
    }

    @Override
    public String getReporter() {
        String reporter =  getValue(getChiledObject(mJsonFields, "reporter"), "name");
        return reporter == null ? "" : reporter;
    }

    @Override
    public String getComponent() {
        try {
            JSONArray array = mJsonFields.getJSONArray("components");
            String components = "";
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item != null) {
                    String component = getValue(item, "name");
                    components += "," + component;
                }
            }
            return components.substring(1);
        } catch (JSONException e) {
            Log.error(getClass(),"[JsonJiraIssueParser]getComponent ", e);
        }
        return null;
    }

    @Override
    public Date getUpdatedTime() {
        String updated = getValue(mJsonFields, "updated");
        updated = updated.replace("T", " ");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz");
        try {
            return formatter.parse(updated);
        } catch (ParseException e) {
            Log.error(getClass(),"[JsonJiraIssueParser]getUpdatedTime ", e);
        }
        return null;
    }

}
