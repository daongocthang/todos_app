package com.standalone.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.standalone.core.utils.DateUtil;
import com.standalone.core.utils.StrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class Dao<T> {
    protected final String tableName;
    protected final SQLiteDatabase db;
    final Class<T> cls;

    public static String getTimestamp() {
        return DateUtil.toString("MM-dd-yyyy HH:mm:ss", DateUtil.now());
    }

    public static <T> Dao<T> of(Class<T> cls) {
        return new Dao<>(cls);
    }

    Dao(Class<T> cls) {
        this.cls = cls;
        this.db = DatabaseManager.getInstance().getDb();
        tableName = StrUtil.pluralize(cls.getSimpleName().toLowerCase());
        createTableIfNotExist();
    }

    public Dao() {
        this.cls = getClassType();
        this.db = DatabaseManager.getInstance().getDb();
        tableName = StrUtil.pluralize(cls.getSimpleName().toLowerCase());
        createTableIfNotExist();
    }

    public void clear() {
        db.execSQL("DELETE FROM " + tableName);
    }

    public void create(T t) {
        db.insert(tableName, null, toValues(t));
    }

    public List<T> getAll() {
        List<T> rows = new ArrayList<>();

        Cursor curs = db.rawQuery("SELECT * FROM " + tableName, null);
        if (curs != null) {
            if (curs.moveToFirst()) {
                do {
                    rows.add(getValues(curs));
                } while (curs.moveToNext());
            }

            curs.close();
        }

        return rows;
    }

    public T get(long id) {
        T t = null;

        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE _id = ?", new String[]{String.valueOf(id)});
        if (curs != null) {
            if (curs.moveToFirst()) {
                t = getValues(curs);
            }
            curs.close();
        }

        return t;
    }

    public void update(long id, T t) {
        db.update(tableName, toValues(t), "_id = ?", new String[]{String.valueOf(id)});
    }

    public void delete(long id) {
        db.delete(tableName, "_id = ?", new String[]{String.valueOf(id)});
    }

    public long getCount() {
        return DatabaseUtils.queryNumEntries(db, tableName);
    }


    protected ContentValues toValues(T t) {
        ContentValues cv = new ContentValues();
        accessDeclaredFields(new FieldAccessor() {
            @Override
            public void onAccess(Field field, boolean primary) throws IllegalAccessException {
                if (primary) return;

                Object value = field.get(t);
                Class<?> type = field.getType();
                String fieldName = StrUtil.camelToSnake(field.getName());
                if (value == null) return;

                if (canAssign(type, int.class)) {
                    cv.put(fieldName, (int) value);
                } else if (canAssign(type, long.class)) {
                    cv.put(fieldName, (long) value);
                } else if (canAssign(type, double.class)) {
                    cv.put(fieldName, (double) value);
                } else if (canAssign(type, boolean.class)) {
                    cv.put(fieldName, (boolean) value);
                } else {
                    if (fieldName.contains("updated_at")) {
                        value = getTimestamp();
                    }
                    cv.put(fieldName, value.toString());
                }

            }
        });
        return cv;
    }

    protected T getValues(Cursor cursor) {
        try {
            T t = cls.newInstance();
            accessDeclaredFields(new FieldAccessor() {
                @Override
                public void onAccess(Field field, boolean primary) throws IllegalAccessException {
                    Object value = null;
                    Class<?> type = field.getType();
                    String fieldName = StrUtil.camelToSnake(field.getName());
                    int colIndex = cursor.getColumnIndex((primary ? "_" : "") + fieldName);
                    if (canAssign(type, int.class)) {
                        value = cursor.getInt(colIndex);
                    } else if (canAssign(type, long.class)) {
                        value = cursor.getLong(colIndex);
                    } else if (canAssign(type, double.class)) {
                        value = cursor.getDouble(colIndex);
                    } else if (canAssign(type, boolean.class)) {
                        value = cursor.getInt(colIndex) > 0;
                    } else if (canAssign(type, String.class)) {
                        value = cursor.getString(colIndex);
                    }

                    if (value != null) field.set(t, value);
                }
            });
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void accessDeclaredFields(FieldAccessor accessor) {
        try {
            for (Field field : cls.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                if (column == null) continue;
                accessor.onAccess(field, column.primary());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean canAssign(Class<?> a, Class<?> b) {
        return a.isAssignableFrom(b);
    }

    private void createTableIfNotExist() {
        List<String> cols = new ArrayList<>();
        accessDeclaredFields(new FieldAccessor() {
            @Override
            public void onAccess(Field field, boolean primary) throws IllegalAccessException {
                StringBuilder builder = new StringBuilder();
                String fieldName = StrUtil.camelToSnake(field.getName());
                builder.append(primary ? "_" : "").append(fieldName).append(" ");
                Class<?> type = field.getType();
                if (canAssign(type, int.class) || canAssign(type, long.class) || canAssign(type, boolean.class)) {
                    builder.append("INTEGER");
                } else if (canAssign(type, double.class) || canAssign(type, float.class)) {
                    builder.append("REAL");
                } else if (canAssign(type, String.class)) {
                    builder.append("TEXT");
                } else {
                    throw new DataTypeException();
                }

                if (primary) {
                    builder.append(" PRIMARY KEY AUTOINCREMENT");
                }

                cols.add(builder.toString());
            }
        });

        String sql = String.format("CREATE TABLE IF NOT EXISTS %s(%s);", tableName, String.join(", ", cols));
        db.execSQL(sql);
    }

    static class DataTypeException extends RuntimeException {

    }

    interface FieldAccessor {
        void onAccess(Field field, boolean primary) throws IllegalAccessException;
    }


    @SuppressWarnings("unchecked")
    public Class<T> getClassType() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }
}
