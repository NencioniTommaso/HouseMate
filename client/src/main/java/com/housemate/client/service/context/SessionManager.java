package com.housemate.client.service.context;

import java.util.prefs.Preferences;

public class SessionManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

    private static final String KEY_TOKEN = "jwt_token";

    public static void saveSession(String token, String userId, String householdId) {
        prefs.put(KEY_TOKEN, token);
    }

    public static String getToken() { return prefs.get(KEY_TOKEN, null); }

    public static void clearSession() {
        prefs.remove(KEY_TOKEN);
    }
}