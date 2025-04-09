package com.example.giaothong.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.giaothong.R;

/**
 * Lớp trợ giúp phát âm thanh trong ứng dụng
 */
public class SoundHelper {
    private static final String TAG = "SoundHelper";
    
    /**
     * Phát âm thanh khi trả lời đúng
     * @param context Context để truy cập resource
     */
    public static void playCorrectSound(Context context) {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.correct);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                });
                mediaPlayer.start();
                Log.d(TAG, "Đang phát âm thanh 'correct'");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi phát âm thanh 'correct': " + e.getMessage());
        }
    }
    
    /**
     * Phát âm thanh khi trả lời sai
     * @param context Context để truy cập resource
     */
    public static void playWrongSound(Context context) {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.wrong);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                });
                mediaPlayer.start();
                Log.d(TAG, "Đang phát âm thanh 'wrong'");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi phát âm thanh 'wrong': " + e.getMessage());
        }
    }
} 