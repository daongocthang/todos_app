package com.standalone.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

/**
 * Requirement:
 * <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
 * tools:ignore="ScopedStorage" />
 **/
public class StorageUtil {
    static final int PERMISSION_REQUEST_CODE = 101;

    static String TAG = StorageUtil.class.getSimpleName();

    public static File getDefaultStorage(Context context) {
        String appName = getAppName(context);
        File file = new File(Environment.getExternalStorageDirectory(), appName);
        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e(TAG, "Cannot create a new folder");
            }
        }

        return file;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static File getRemovableStorage(Context context) {
        String appName = getAppName(context);
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumeList = storageManager.getStorageVolumes();
        StorageVolume storageVolume = storageVolumeList.get(1);
        File file = new File(storageVolume.getDirectory(), appName);
        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e(TAG, "Cannot create a new folder");
            }
        }

        return file;
    }

    public static boolean hasSDCard() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static void requirePermission(Activity activity) {
        if (checkPermission(activity)) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("packages:%s", activity.getPackageName())));
                activity.startActivityIfNeeded(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityIfNeeded(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    static boolean checkPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        int resultCode = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return resultCode == PackageManager.PERMISSION_GRANTED;
    }

    static String getAppName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }
}
