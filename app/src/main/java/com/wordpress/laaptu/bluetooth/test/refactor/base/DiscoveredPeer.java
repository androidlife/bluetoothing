package com.wordpress.laaptu.bluetooth.test.refactor.base;

public interface DiscoveredPeer {
    int PRIORITY_HIGHEST = 0;
    int PRIORITY_INTERMEDIATE = 1;
    int PRIORITY_LOWEST = 2;

    public static interface ConnectionListener {
        public void onAccepted();

        public void onDeclined();
    }

    public String getName();

    public int getPicture();

    public String getStatus();

    public void connectTo(ConnectionListener connectioListener);

    public int getPriority();

    public void release();

    String getUniqueIdentifier();
    boolean isServer();

    boolean equals(Object obj);

    int hashCode();
}