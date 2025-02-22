package com.standalone.todos.local.todos;

import android.content.ContentValues;
import android.database.Cursor;

import com.standalone.core.dao.Dao;
import com.standalone.core.utils.DateUtil;
import com.standalone.todos.SyncManager;

import java.util.List;

public class TodoDao extends Dao<Todo> {

    public List<Todo> getAllUnsynced() {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE last_synced_time < ?", new String[]{String.valueOf(DateUtil.now().getTime() - SyncManager.SYNCED_LATENCY)});
        return parseList(curs);
    }

    public Todo getByServerId(long id) {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE server_id = ?", new String[]{String.valueOf(id)});
        return parse(curs);
    }

    public void softDelete(long id) {
        Todo todo = this.get(id);
        if (todo == null) return;
        ContentValues cv = new ContentValues();
        cv.put("is_deleted", true);
        this.update(id, cv);
    }

    public void notifySynced(long id) {
        Todo todo = this.get(id);
        if (todo == null) return;
        ContentValues cv = new ContentValues();
        cv.put("last_synced_time", DateUtil.now().getTime());
        this.update(id, cv);
    }
}
