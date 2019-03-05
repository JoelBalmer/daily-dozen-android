package org.nutritionfacts.dailydozen.util;

import android.content.Context;
import android.content.SharedPreferences;

public class DarkModeUtil {
    public static boolean getDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("darkModePrefs", context.MODE_PRIVATE);
        return prefs.getBoolean("darkMode", false);
    }

    public static void setDarkMode(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("darkModePrefs", context.MODE_PRIVATE).edit();
        editor.putBoolean("darkMode", !getDarkMode(context));
        editor.commit();
    }

    public static int getLayoutId(Context context, String layoutName) {
        String suffix = getDarkMode(context) ? "_dark" : "";
        int resourceId = context.getResources().getIdentifier(layoutName + suffix, "layout", context.getPackageName());
        return resourceId;
    }
}
