package com.standalone.todos.local.todos;

import com.standalone.core.dao.Column;
import com.standalone.core.dao.Dao;
import com.standalone.core.utils.DateUtil;

import java.util.Date;

public class Todo {
    private static final String DATE_FORMAT = "YYYY-MM-DD HH:mm:ss";

    @Column(primary = true)
    private long id;

    @Column
    public long serverId = -1;

    @Column
    public String content;

    @Column
    public boolean completed;

    @Column
    public boolean isDeleted;

    @Column
    private String createdAt;

    @Column
    private String updatedAt;


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

    public Date createdAt() {
        return DateUtil.parseTime(DATE_FORMAT, createdAt);
    }

    public Date updatedAt() {
        return DateUtil.parseTime(DATE_FORMAT, updatedAt);
    }

}
