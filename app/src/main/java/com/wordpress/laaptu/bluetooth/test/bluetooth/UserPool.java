package com.wordpress.laaptu.bluetooth.test.bluetooth;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class UserPool {
    private static Context contextU;

    public static class User {
        public User(String name, int smallportraitid, int portraitid) {
            this.name = name;
            this.smallportraitid = smallportraitid;
            this.portraitid = portraitid;
        }

        public String name;
        public int smallportraitid;
        public int portraitid;
        public static final String STATUS_ONLINE = "Online", STATUS_BUSY = "Busy", STATUS_OFFLINE = "Offline";

    }

    private static Random randomizer;
    private static HashMap<String, User> userMap;
    private static ArrayList<User> unusedUsers;
    private static final User[] predefinedUsers = new User[]{

    };
//    private static final User me = new User("Me", R.drawable.user_me, R.drawable.user_me);

    /*public static User getMe() {
        return me;
    }*/

    private static final User[] offlineUsers = new User[]{

    };

    public static User[] getOfflineUsers() {
        return offlineUsers;
    }

    //private static final User busyUser = new User(LiveTouchApplication.getContext().getString(R.string.stream4), R.drawable.stream_4, R.drawable.stream_4);

    public static User getBusyUser() {
        return null;
    }

    static {
        randomizer = new Random(System.nanoTime());
        userMap = new HashMap<String, User>();
        unusedUsers = new ArrayList<User>(Arrays.asList(predefinedUsers));
    }

    public static void release() {
        randomizer = null;
        if (userMap != null) {
            userMap.clear();
            userMap = null;
        }
        if (unusedUsers != null) {
            unusedUsers.clear();
            unusedUsers = null;
        }
    }

    public static User getUserByName(String name) {
        if (userMap.containsKey(name)) {
            return userMap.get(name);
        }

        User random = getRandomUser();
        if (random != null) {
            userMap.put(name, random);
        }
        return random;
    }

    public static void releaseUser(User user, String name) {
        userMap.remove(name);
        unusedUsers.add(user);
    }

    public static void setContext(Context context) {
        contextU = context;

    }

    private static User getRandomUser() {
        if (!unusedUsers.isEmpty()) {
            int index = randomizer.nextInt(unusedUsers.size());
            User random = unusedUsers.get(index);
            unusedUsers.remove(index);
            return random;
        }
        return null;
    }
}
