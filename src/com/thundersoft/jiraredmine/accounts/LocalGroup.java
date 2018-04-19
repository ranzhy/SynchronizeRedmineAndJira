package com.thundersoft.jiraredmine.accounts;

import java.util.ArrayList;
import java.util.List;

public class LocalGroup {

    final private String mName;
    private LocalUser mLeader;
    private ArrayList<LocalUser> mUsers = new ArrayList<LocalUser>();

    protected LocalGroup(String name) {
        mName = name;
    }

    public String getGroupName() {
        return mName;
    }

    protected void setLeader(LocalUser leader) {
        mLeader = leader;
    }

    public LocalUser getLeader() {
        return mLeader;
    }

    protected void add(LocalUser user) {
        if (mLeader == null) {
            setLeader(user);
        }
        if (!mUsers.contains(user)) {
            mUsers.add(user);
            user.setGroup(this);
        }
    }

    public List<LocalUser> getUsers() {
        ArrayList<LocalUser> users = new ArrayList<LocalUser>();
        users.addAll(mUsers);
        return users;
    }
 
    public String toString() {
        return "[" + mLeader + "@" + getGroupName() + "]";
    }
}
