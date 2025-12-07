package com.example.mytraveldiary.utils.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mytraveldiary.utils.constants.Constants;

/**
 * Helper class for runtime permissions
 */
public class PermissionHelper {

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            Constants.PERMISSION_LOCATION);
    }

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.CAMERA},
            Constants.PERMISSION_CAMERA);
    }

    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                Constants.PERMISSION_STORAGE);
        } else {
            ActivityCompat.requestPermissions(activity,
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                Constants.PERMISSION_STORAGE);
        }
    }
}
