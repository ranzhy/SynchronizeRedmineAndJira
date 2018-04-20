package com.thundersoft.jiraredmine.accounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.UserManager;
import com.taskadapter.redmineapi.bean.User;
import com.thundersoft.jiraredmine.config.ContentConfig;
import com.thundersoft.jiraredmine.config.SystemConfig;
import com.thundersoft.jiraredmine.logger.Log;

public class AccountsManager {

    private UserManager mUserManager;
    private ContentConfig mContentConfig;

    private HashMap<String, LocalUser> mUsers = new HashMap<String, LocalUser>();
    private HashMap<String, LocalGroup> mGroups = new HashMap<String, LocalGroup>();

    public AccountsManager(UserManager userMgr) {
        mUserManager = userMgr;
        mContentConfig = SystemConfig.getInstance().getContentConfig();
        initialize();
    }

    private void initialize() {
        List<User> users = loadRedmineUsers();
        for (User user : users) {
            loadLocalUserConfig(user);
        }
    }

    private List<User> loadRedmineUsers() {
        try {
            return mUserManager.getUsers();
        } catch (RedmineException e) {
            throw new RuntimeException("Load redmine users failed, Abort", e);
        }
    }

    private void loadLocalUserConfig(User user) {
        String mail = user.getMail();
        String id = mail.split("\\@")[0].trim();
        String jiraIds = (String) mContentConfig.get(ContentConfig.USERS_CONFIG_KEY + id);
        if (jiraIds != null && !jiraIds.trim().isEmpty()) {
            String[] ids = jiraIds.split(",");
            for (String jiraId : ids) {
                if (jiraId != null && !jiraId.trim().isEmpty()) {
                    addMatchedUser(user, jiraId.trim(), true);
                }
            }
        }

        // Self is added at last to ensure the leader is correct
        String jiraId = id;
        if (mail.endsWith("thundersoft.com")) {
            jiraId += ".ts";
        }
        addMatchedUser(user, jiraId, false);
    }

    private void addMatchedUser(User user, String jiraId, boolean config) {
        String mail = user.getMail();
        LocalUser localUser = new LocalUser(user, jiraId);
        putUser(jiraId, localUser, config);

        String groupName = user.getFirstName();
        if (mail.endsWith("thundersoft.com")) {
            if (!groupName.startsWith("TS-")) {
                groupName = "TS-PM";
            }
        } else {
            groupName = "PMC";
        }
        LocalGroup group = mGroups.get(groupName);
        if (group == null) {
            group = new LocalGroup(groupName);
            mGroups.put(groupName, group);
        }
        String leaderMail = (String) mContentConfig.get(ContentConfig.GROUP_CONFIG_KEY + groupName);
        if (mail.equals(leaderMail)) {
            group.setLeader(localUser);
        }
        group.add(localUser);
        Log.debug(getClass(), "Add " + jiraId + "(" + mail + ")" + user.getFullName() + " to " + group);
    }

    private void putUser(String id, LocalUser user, boolean force) {
        if (id.endsWith(".ts")) {
            id = id.replace(".ts", "");
        }
        if (mUsers.get(id) == null || force) {
            mUsers.put(id, user);
        }
    }

    public LocalUser getUserByJiraId(String id) {
        if (id.endsWith(".ts")) {
            id = id.replace(".ts", "");
        }
        return mUsers.get(id);
    }

    public LocalUser getUserByRedmineUserId(int userId) {
        try {
            User user = mUserManager.getUserById(userId);
            String mail = user.getMail();
            mail = mail.split("\\@")[0];
            return getUserByJiraId(mail);
        } catch (RedmineException e) {
            // TODO Auto-generated catch block
            Log.error(getClass(), "", e);
            return null;
        }
    }

    public LocalGroup getGroupByName(String group) {
        return mGroups.get(group);
    }
}
