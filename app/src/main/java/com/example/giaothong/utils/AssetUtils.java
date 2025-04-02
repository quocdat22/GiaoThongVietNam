package com.example.giaothong.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling assets
 */
public class AssetUtils {
    private static final String TAG = "AssetUtils";

    /**
     * List all files in an assets directory
     * @param context Application context
     * @param directory Directory path in assets
     * @return List of file paths
     */
    public static List<String> listAssetFiles(Context context, String directory) {
        List<String> filePaths = new ArrayList<>();
        
        try {
            String[] fileList = context.getAssets().list(directory);
            if (fileList != null) {
                for (String file : fileList) {
                    // Check if it's a file or directory
                    try {
                        // If this is a directory, list will not be empty
                        String[] subFiles = context.getAssets().list(directory + "/" + file);
                        if (subFiles != null && subFiles.length > 0) {
                            // It's a directory, recursively list files
                            List<String> subFilePaths = listAssetFiles(context, directory + "/" + file);
                            filePaths.addAll(subFilePaths);
                        } else {
                            // It's a file
                            filePaths.add(directory + "/" + file);
                        }
                    } catch (IOException e) {
                        // Assume it's a file
                        filePaths.add(directory + "/" + file);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing files in assets/" + directory, e);
        }
        
        return filePaths;
    }

    /**
     * Load a bitmap from assets
     * @param context Application context
     * @param filePath File path in assets
     * @return Bitmap or null if error
     */
    public static Bitmap loadBitmapFromAssets(Context context, String filePath) {
        try {
            InputStream is = context.getAssets().open(filePath);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from assets/" + filePath, e);
            return null;
        }
    }
} 