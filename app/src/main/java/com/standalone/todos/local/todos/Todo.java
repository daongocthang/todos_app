package com.standalone.todos.local.todos;

import com.standalone.core.dao.Column;
import com.standalone.core.dao.Dao;

public class Todo {
    @Column(primary = true)
    private long id;

    @Column
    private String content;

    @Column
    private boolean completed;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }


}
