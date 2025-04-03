package com.example.giaothong.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing application themes
 */
public class ThemeUtils {

    /**
     * Enable dark mode according to the setting
     * @param isDarkMode true to enable dark mode, false for light mode
     */
    public static void setDarkMode(boolean isDarkMode) {
        int mode = isDarkMode ? 
                AppCompatDelegate.MODE_NIGHT_YES : 
                AppCompatDelegate.MODE_NIGHT_NO;
        
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    
    /**
     * Toggle between dark and light mode
     * @param activity The current activity
     * @param prefsManager SharedPreferencesManager to save theme setting
     * @return new dark mode state
     */
    public static boolean toggleDarkMode(Activity activity, SharedPreferencesManager prefsManager) {
        boolean isDarkMode = !prefsManager.isDarkMode();
        prefsManager.setDarkMode(isDarkMode);
        setDarkMode(isDarkMode);
        return isDarkMode;
    }
    
    /**
     * Apply saved theme setting from preferences
     * @param prefsManager SharedPreferencesManager containing settings
     */
    public static void applyThemeFromPreferences(SharedPreferencesManager prefsManager) {
        setDarkMode(prefsManager.isDarkMode());
    }
    
    /**
     * Check if the current system is in dark mode
     * @param context Application context
     * @return true if in dark mode, false otherwise
     */
    public static boolean isSystemInDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
} 