package com.thundersoft.jiraredmine.issues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class JiraClient {

    private static JiraClient INSTANCE = new JiraClient();
    
    public static JiraClient getInstance() {
        return INSTANCE;
    }

    private JiraClient() {
    }

    public String loadData(String url) {
        return doLoad(url);
    }

    
    private String doLoad(String url) {
        HttpURLConnection conn = null;
        OutputStream out = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            out = doPreload(conn);
            if (conn == null) {
                return null;
            }
            return doRead(conn);
        } catch (IOException e) {
            Log.error(getClass(), "", e);
        } finally {
            try {
                if (out != null ) out.close();
            } catch (IOException e) {
                Log.error(getClass(), "", e);
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;

    }

    private String doRead(HttpURLConnection conn) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.error(getClass(), "", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.error(getClass(), "", e);
                }
            }
            conn.disconnect();
        }

    }

    private OutputStream doPreload(HttpURLConnection conn) {
        OutputStream out = null;
        try {
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
            conn.setRequestMethod("GET");

            ServerConfig config = SystemConfig.getInstance().getServerConfig();
            String user = config.getJiraLoginUser();
            String pwd = config.getJiraLoginPasswd();

            String authKey = "Basic " + Base64.encodeBase64String(
                    (user + ':' + pwd).getBytes("UTF-8")).trim();
            conn.setRequestProperty("Authorization", authKey);
//            out = conn.getOutputStream();
//            writePostData(out);
//            return out;
            return null;
        } catch (IOException e) {
            Log.error(getClass(), "", e);
            return null;
        }
    }

    private String postLoginData() {
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        String user = config.getJiraLoginUser();
        String pwd = config.getJiraLoginPasswd();
        return "os_username=" + user + "&os_password=" + pwd;
    }

    private void writePostData(OutputStream out) {
        PrintWriter print = new PrintWriter(out);
        print.print(postLoginData());
        print.flush();
    }
}
