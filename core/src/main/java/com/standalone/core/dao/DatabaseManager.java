package com.standalone.core.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.standalone.core.App;
import com.standalone.core.utils.NumberUtil;
import com.standalone.core.utils.StorageUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class DatabaseManager extends SQLiteOpenHelper {
    static final String TAG = DatabaseManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    static DatabaseManager instance;
    final Context context;

    DatabaseManager(Context context, String dbName, int version) {
        super(context, dbName, null, version);
        this.context = context;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            Properties config = App.loadEnv();
            String dbName = config.getProperty("DB_NAME");
            if (dbName == null) {
                throw new IllegalArgumentException("Not found name of database ");
            }
            int dbVersion = NumberUtil.toInt(config.getProperty("DB_VERSION"), 1);
            instance = new DatabaseManager(App.getContext(), dbName, dbVersion);
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        dropTables(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @NotNull
    public SQLiteDatabase getDb() {
        return getWritableDatabase();
    }

    @SuppressLint("Range")
    void dropTables(SQLiteDatabase db) {
        db.beginTransaction();
        try (Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type IS 'table'", null)) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String tableName = cursor.getString(cursor.getColumnIndex("name"));
                        db.execSQL("DROP TABLE IF EXISTS " + tableName);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            db.endTransaction();
        }
    }

    public void backup() {
        try {
            File dir = getExtStorage(context);
            getDb().close();
            File src, dst;
            if (dir.canWrite()) {
                src = context.getDatabasePath(getDatabaseName());
                dst = new File(dir, getDatabaseName());
                transfer(src, dst);
            }
        } catch (IOException e) {
            Log.e(TAG, "Write data failed");
        }
    }

    public void restore() {
        try {
            File dir = getExtStorage(context);
            getDb().close();
            File src, dst;
            if (dir.canRead()) {
                src = new File(dir, getDatabaseName());
                dst = context.getDatabasePath(getDatabaseName());
                transfer(src, dst);
            }
        } catch (IOException e) {
            Log.e(TAG, "Read data failed");
        }
    }

    File getExtStorage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return StorageUtil.getRemovableStorage(context);
        }

        return StorageUtil.getDefaultStorage(context);
    }


    void transfer(File src, File dst) throws IOException {
        //noinspection resource
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        //noinspection resource
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        dstChannel.close();
    }
}
