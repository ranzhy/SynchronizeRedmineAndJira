package com.thundersoft.jiraredmine.accounts;

import com.taskadapter.redmineapi.bean.User;

public class LocalUser {

    private User mRedmineUser;
    private String mJiraUserKey;
    private LocalGroup mGroup;

    protected LocalUser(User redmine, String jiraKey/*, LocalGroup group*/) {
        mRedmineUser = redmine;
        mJiraUserKey = jiraKey;
//        mGroup = group;
//        group.add(this);
    }

    public String getGroupName() {
        // First name as group name
        return mGroup != null ? mGroup.getGroupName() : null;
    }

    public String getUserName() {
        return mRedmineUser.getLastName();
    }

    public String getFullName() {
        return mRedmineUser.getFullName();
    }

    public int getRedmineUserId() {
        return mRedmineUser.getId();
    }

    public String getJiraUserId() {
        return mJiraUserKey;
    }

    public LocalGroup getGroup() {
        return mGroup;
    }

    protected void setGroup(LocalGroup group) {
        if (mGroup == null) {
            mGroup = group;
            group.add(this);
        }
    }
 
    public String toString() {
        return "[" + mJiraUserKey + "@" + getGroupName() + "]";
    }
}
