package com.housemate.client.service.context;

import java.util.prefs.Preferences;

public class SessionManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_HOUSEHOLD_ID = "household_id";

    public static void saveSession(String token, String userId, String householdId) {
        prefs.put(KEY_TOKEN, token);
        prefs.put(KEY_USER_ID, userId);

        if (householdId != null && !householdId.isEmpty()) {
            prefs.put(KEY_HOUSEHOLD_ID, householdId);
        } else {
            prefs.remove(KEY_HOUSEHOLD_ID);
        }
    }

    public static String getToken() { return prefs.get(KEY_TOKEN, null); }
    public static String getUserId() { return prefs.get(KEY_USER_ID, null); }
    public static String getHouseholdId() { return prefs.get(KEY_HOUSEHOLD_ID, null); }

    public static void clearSession() {
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_USER_ID);
        prefs.remove(KEY_HOUSEHOLD_ID);
    }
}