package com.example.giaothong.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Lớp tiện ích chứa các phương thức hỗ trợ
 */
public class Utils {

    /**
     * Hiển thị bàn phím cho view đang có focus
     * @param context Context
     * @param view View cần hiển thị bàn phím
     */
    public static void showKeyboard(Context context, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            
            view.postDelayed(() -> {
                InputMethodManager immDelayed = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                immDelayed.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }, 100);
        }
    }
    
    /**
     * Ẩn bàn phím
     * @param activity Activity hiện tại
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
} 