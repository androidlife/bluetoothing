package com.wordpress.laaptu.bluetooth.test.refactor;

import android.content.Context;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.refactor.base.DiscoveredPeer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class UserPool {
    private static Context contextU;

    public static String getDefaultUserName() {
        return "DefaultUser";
    }

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
            new User("User1", R.drawable.stream_1, R.drawable.stream_1),
            new User("User2", R.drawable.stream_2, R.drawable.stream_2),
            new User("User3", R.drawable.stream_3, R.drawable.stream_3),
            new User("User5", R.drawable.stream_5, R.drawable.stream_5),
            new User("User8", R.drawable.stream_8, R.drawable.stream_8),
            new User("User9", R.drawable.stream_9, R.drawable.stream_9),
            new User("User10", R.drawable.stream_10, R.drawable.stream_10),

    };
    private static final User[] offlineUsers = new User[]{
            new User("User6", R.drawable.stream_6, R.drawable.stream_6),
            new User("User7", R.drawable.stream_7, R.drawable.stream_7),
    };

    public static User[] getOfflineUsers() {
        return offlineUsers;
    }

    private static final User busyUser = new User("User4", R.drawable.stream_4, R.drawable.stream_4);

    public static User getBusyUser() {
        return busyUser;
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

    //static user
    public static class StaticPeer implements DiscoveredPeer {

        private User user;
        private String status;
        private int priority;

        public StaticPeer(User user, String status, int priority) {
            this.user = user;
            this.status = status;
            this.priority = priority;
        }

        @Override
        public String getName() {
            return user.name;
        }

        @Override
        public int getPicture() {
            return user.portraitid;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public void connectTo(ConnectionListener connectioListener) {
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void release() {
        }

        @Override
        public String getUniqueIdentifier() {
            return null;
        }

        @Override
        public boolean isServer() {
            return false;
        }
    }
}
