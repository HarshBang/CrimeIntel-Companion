package com.example.crimeintelcompanion.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {
    private static final String PREF_NAME = "CrimeIntelPrefs";
    private static final String KEY_IMEI  = "IMEI";
    private static final String KEY_BADGE = "BadgeNumber";
    private static final String KEY_MPIN  = "MPIN";
    private static final String KEY_NAME  = "UserName";

    private final SharedPreferences prefs;

    public Pref(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setImei(String imei) { prefs.edit().putString(KEY_IMEI, imei).apply(); }
    public String getImei() { return prefs.getString(KEY_IMEI, null); }
    public boolean isImeiStored() { return getImei() != null && !getImei().isEmpty(); }

    public void setBadge(String badge) { prefs.edit().putString(KEY_BADGE, badge).apply(); }
    public String getBadge() { return prefs.getString(KEY_BADGE, null); }
    public boolean isBadgeStored() { return getBadge() != null && !getBadge().isEmpty(); }

    public void setMpin(String mpin) { prefs.edit().putString(KEY_MPIN, mpin).apply(); }
    public String getMpin() { return prefs.getString(KEY_MPIN, null); }
    public boolean isMpinSet() { return getMpin() != null && !getMpin().isEmpty(); }

    public void setName(String name) { prefs.edit().putString(KEY_NAME, name).apply(); }
    public String getName() { return prefs.getString(KEY_NAME, null); }

    public void clear() { prefs.edit().clear().apply(); }
}
