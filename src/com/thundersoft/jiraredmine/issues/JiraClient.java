package com.thundersoft.jiraredmine.issues;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.thundersoft.jiraredmine.config.ServerConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class JiraClient {

    private static final int DEFAULT_BUFFERF_SIZE = 1024 * 10; // 1M

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
        InputStream in = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            if (conn == null) {
                return null;
            }
            doPreload(conn);
            in = doConnection(conn);
            return doRead(in);
        } catch (IOException e) {
            Log.error(getClass(), "", e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                Log.error(getClass(), "", e);
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;

    }

    private InputStream doConnection(HttpURLConnection conn) {
        try {
            Log.debug(getClass(), " >> " + conn.getRequestMethod() + " " + conn.getURL());
            Map<String, List<String>> request = conn.getRequestProperties();
            for (String key :  request.keySet()) {
                Log.debug(getClass(), " >> " + key + " : " + request.get(key).get(0));
            }

            conn.connect();
            Map<String, List<String>> resp = new HashMap<String, List<String>>(conn.getHeaderFields());
            Log.debug(getClass(), " << " + resp.remove(null).get(0));
            for (String key :  resp.keySet()) {
                Log.debug(getClass(), " << " + key + " : " + resp.get(key).get(0));
            }
            return conn.getInputStream();
        } catch (IOException e) {
            Log.error(getClass(), "Connecting failed : " + conn.getURL(), e);
        }
        return null;
    }

    private String doRead(InputStream in) throws IOException {
        int len = 0;
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFERF_SIZE);
        while ((len = in.read(buffer.array(), buffer.position(), buffer.remaining())) >= 0) {
            buffer.position(buffer.position() + len);
            if (!buffer.hasRemaining()) {
                ByteBuffer tmp = buffer;
                buffer = ByteBuffer.allocate(DEFAULT_BUFFERF_SIZE + buffer.capacity());
                buffer.put(tmp.array());
            }
        };
        String content = new String(buffer.array(), 0, buffer.position(),"UTF-8");
        Log.debug(getClass(), "<< " + content);
        return content;
    }

    private void doPreload(HttpURLConnection conn) {
        ServerConfig config = SystemConfig.getInstance().getServerConfig();
        String user = config.getJiraLoginUser();
        String pwd = config.getJiraLoginPasswd();

        try {
            String authKey = "Basic " + Base64.encodeBase64String(
                    (user + ':' + pwd).getBytes("UTF-8")).trim();

            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", authKey);
        } catch (IOException e) {
            Log.error(getClass(), "", e);
        }
    }
}
