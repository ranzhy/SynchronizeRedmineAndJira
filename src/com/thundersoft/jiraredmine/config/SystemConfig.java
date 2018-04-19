package com.thundersoft.jiraredmine.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.taskadapter.redmineapi.CustomFieldManager;
import com.taskadapter.redmineapi.RedmineManager;
import com.thundersoft.jiraredmine.accounts.AccountsManager;
import com.thundersoft.jiraredmine.issues.IssuePriorityManager;
import com.thundersoft.jiraredmine.issues.IssueStatusManager;
import com.thundersoft.jiraredmine.logger.Log;
import com.thundersoft.jiraredmine.sync.AbstractIssueHandler;
import com.thundersoft.jiraredmine.sync.DefaultIssueHandler;

public class SystemConfig extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 2490831944009972746L;

    public static final String DEFAULT_SYSTEM_CONFIG = "systemConfig.properties";
    public static final String DEFAULT_SERVER_CONFIG = "servers.properties";
    public static final String DEFAULT_CONTENT_CONFIG = "content.properties";
    public static final String DEFAULT_ISSUE_HANDLER = "com.thundersoft.jiraredmine.sync.DefaultIssueHandler";

    public static final String SERVERS_CONFIG_KEY = "servers.config";
    public static final String CONTENT_CONFIG_KEY = "content.config";
    public static final String ISSUE_HANDLER_CONFIG = "issue.handler";

    private static final SystemConfig INSTANCE = new SystemConfig();

    private ServerConfig mServerConfig;
    private ContentConfig mContentConfig;

    public static SystemConfig getInstance() {
        return INSTANCE;
    }

    private SystemConfig() {
        super();
    }

    protected File getConfig() {
        return new File(DEFAULT_SYSTEM_CONFIG);
    }

    private void load() {
        InputStream in = null;
        try {
            in = new FileInputStream(getConfig());
            load(in);
        } catch (IOException e) {
            Log.error(getClass(), "", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.error(getClass(), "", e);
                }
            }
        }
        
    }

    private void loadIfNeeded() {
        if (isEmpty()) {
            load();
        }
    }

    private String getConfig(String key, String def) {
        loadIfNeeded();
        String server = (String) get(key);
        if (server == null || server.trim().isEmpty()) {
            server = def;
            setProperty(key, server);
        }
        return server;
    }

    public AbstractIssueHandler createIssueHandler(RedmineManager redmine) {
        AbstractIssueHandler handler = null;
        String className = getConfig(ISSUE_HANDLER_CONFIG, DEFAULT_ISSUE_HANDLER);
        if (className != null && !className.trim().isEmpty()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz != null) {
                    Constructor constructor = clazz.getConstructor(RedmineManager.class);
                    Object obj = constructor.newInstance(redmine);
                    if (obj instanceof AbstractIssueHandler) {
                        handler = (AbstractIssueHandler) obj;
                    } else {
                        Log.error(getClass(), "issue handler must extends AbstractIssueHandler, Incorrect issue handler: " + obj);
                    }
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
            }
        }
        if (handler == null) {
            Log.info(getClass(), "Will use the default issue handler");
            handler = new DefaultIssueHandler(redmine);
        }
        return handler;
    }

    public ServerConfig getServerConfig() {
        if (mServerConfig == null) {
            mServerConfig = new ServerConfig();
            InputStream in = null;
            try {
                in = new FileInputStream(new File(getConfig(SERVERS_CONFIG_KEY, DEFAULT_SERVER_CONFIG)));
                mServerConfig.load(in);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
                mServerConfig = null;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Log.error(getClass(), "", e);
                    }
                }
            }
        }
        return mServerConfig;
    }

    public ContentConfig getContentConfig() {
        if (mContentConfig == null) {
            mContentConfig = new ContentConfig();
            InputStream in = null;
            try {
                in = new FileInputStream(new File(getConfig(CONTENT_CONFIG_KEY, DEFAULT_CONTENT_CONFIG)));
                mContentConfig.load(in);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.error(getClass(), "", e);
                mContentConfig = null;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Log.error(getClass(), "", e);
                    }
                }
            }
        }
        return mContentConfig;
    }
}
