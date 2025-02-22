package com.standalone.todos;

import android.content.ContentValues;

import com.github.javafaker.Faker;
import com.standalone.core.ext.ApiService;
import com.standalone.core.utils.Json;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.Response;

@RunWith(RobolectricTestRunner.class)
public class SandwichTest {
    private static final String DB_NAME = "todos";
    private static final int DB_VERSION = 1;
    private MainActivity activity;
    private ApiService<Todo> apiService;
    private TodoDao dao;
    private Faker faker;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        apiService = new ApiService<>();
        faker = new Faker();
        dao = new TodoDao();
    }

    @Test
    public void testCompletableFuture_update() {
        Todo t = createTodo();
        apiService.update(1, t).join();
    }

    @Test
    public void testCompletableFuture_fetchData() {
        try {
            for (Todo t : fetchDataFromServer()) {
                echo(t.string());
            }
        } catch (ExecutionException | InterruptedException | IOException e) {
            echo(e.getMessage());
        }
    }

    @Test
    public void testCompletableFuture_create() {
        Todo todo = createTodo();
        echo("@meta: " + todo.string());
        CompletableFuture<Response> cf = apiService.insert(todo);
        try {
            Response response = cf.get();
            if (response.body() == null) throw new IOException();
            Todo t = Json.parse(response.body().string(), Todo.class);
            echo("@then: " + t.string());
        } catch (ExecutionException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCompletableFuture_bulkCreate() {
        List<CompletableFuture<Response>> futures = new ArrayList<>();
        int size = 5;
        for (int i = 0; i < size; i++) {
            Todo todo = createTodo();
            CompletableFuture<Response> cf = apiService.insert(todo);
            futures.add(cf);
        }
        ApiService.join(futures)
                .thenAccept(list -> {
                    for (Response res : list) {
                        if (res.body() != null)
                            echo(res.body().toString());
                    }
                })
                .exceptionally(e -> {
                    throw new RuntimeException(e);
                });

    }

    @Test
    public void testCompletableFuture_join() {
        List<CompletableFuture<Response>> futureList = new ArrayList<>();
        int count = 5;
        while (count > 0) {
            CompletableFuture<Response> cf = apiService.fetchById(count);
            futureList.add(cf);
            count -= 1;
        }


        ApiService.join(futureList).thenAccept(responseList -> {
            for (Response res : responseList) {
                if (res.body() == null) continue;
                try {
                    echo(res.body().string());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            echo("Run successfully.");
        });

    }

    @Test
    public void testDao_create() {
        Todo todo = createTodo();
        dao.create(todo);
        showDataFromLocal();
    }

    @Test
    public void testDao_syncFromServer() {
        try {
            for (Todo t : fetchDataFromServer()) {
                if (t.getId() > 0) continue;
                dao.create(t);
            }
            showDataFromLocal();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void echo(String s) {
        System.out.println(s);
    }

    private List<Todo> fetchDataFromServer() throws IOException, ExecutionException, InterruptedException {
        Response response = apiService.fetchAll().get();
        if (response.body() == null) throw new IOException();
        return Json.parseList(response.body().string(), Todo.class);
    }

    private void showDataFromLocal() {
        for (Todo t : dao.getAll()) {
            echo(t.string());
        }
    }

    private Todo createTodo() {
        return new Todo(faker.lorem().sentence(3));
    }
}
