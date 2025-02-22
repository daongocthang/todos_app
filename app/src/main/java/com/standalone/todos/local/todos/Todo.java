package com.standalone.todos.local.todos;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.standalone.core.dao.Column;
import com.standalone.core.dao.Dao;
import com.standalone.core.utils.DateUtil;
import com.standalone.core.utils.StrUtil;
import com.standalone.todos.SyncManager;

import java.lang.reflect.Field;
import java.util.Date;

public class Todo {

    @JsonIgnore
    @Column(primary = true)
    private long id;

    @JsonProperty("id")
    @Column
    private long serverId;

    @JsonProperty
    @Column
    public String content;

    @JsonProperty
    @Column
    public boolean completed;

    @JsonIgnore
    @Column
    public boolean isDeleted;

    @JsonProperty
    @Column
    private String createdAt;

    @JsonProperty
    @Column
    private String updatedAt;

    @JsonIgnore
    @Column
    private long lastSyncedTime;

    public Todo() {
        init();
    }

    public Todo(String content) {
        this.content = content;
        init();
    }

    private void init() {
        this.completed = false;
        this.createdAt = Dao.getTimestamp();
        this.updatedAt = Dao.getTimestamp();
    }

    public long getId() {
        return id;
    }

    public long getServerId() {
        return serverId;
    }

    public Date createdAt() {
        return DateUtil.parseTime(Dao.DATE_FORMAT, createdAt);
    }

    public Date updatedAt() {
        return DateUtil.parseTime(Dao.DATE_FORMAT, updatedAt);
    }

    public boolean synced() {
        return (DateUtil.now().getTime() - this.lastSyncedTime) <= SyncManager.SYNCED_LATENCY;
    }

    public static ContentValues asContentValues(Todo todo) {
        ContentValues cv = new ContentValues();
        cv.put("server_id", todo.serverId);
        cv.put("content", todo.content);
        cv.put("completed", todo.completed);
        cv.put("created_at", todo.createdAt);
        cv.put("updated_at", todo.updatedAt);
        return cv;
    }

    @NonNull
    public String string() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        try {
            for (Field field : getClass().getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column == null) continue;
                builder.append(field.getName()).append(": ").append(field.get(this)).append("; ");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        builder.append("}");
        return builder.toString();
    }


}
