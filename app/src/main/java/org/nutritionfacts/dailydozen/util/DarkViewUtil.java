package org.nutritionfacts.dailydozen.util;

import android.content.Context;
import android.content.SharedPreferences;

public class DarkViewUtil {
    private static boolean getDarkModeSetting(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("darkModePrefs", context.MODE_PRIVATE);
        boolean restoredSetting = prefs.getBoolean("darkMode", false);
        return restoredSetting;
    }

    public static int inflateLinearLayoutView(Context context, String layoutName) {
        String suffix = getDarkModeSetting(context) ? "_dark" : "";
        int resourceId = context.getResources().getIdentifier(layoutName + suffix, "layout", context.getPackageName());
        return resourceId;
    }
}
