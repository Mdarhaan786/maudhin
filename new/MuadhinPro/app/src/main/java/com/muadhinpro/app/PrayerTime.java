package com.muadhinpro.app;

public class PrayerTime {
    private String name;
    private String time;
    private boolean isNotificationEnabled;

    public PrayerTime(String name, String time) {
        this.name = name;
        this.time = time;
        this.isNotificationEnabled = true;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean enabled) {
        this.isNotificationEnabled = enabled;
    }
}
