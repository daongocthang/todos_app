package com.standalone.todos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.utils.Json;
import com.standalone.todos.local.todos.Todo;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyUnitTest {
    @Test
    public void whenUsingJson_stringify() {
        Todo todo = new Todo("Do something nice for someone I care about");

        try {
            String jsonStr = Json.stringify(todo);
            echo(jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCompletableFuture_runSync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Hello";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
                return "World";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        String res = f1.thenApply(s -> {
            return s + " " + f2.join();
        }).join();
        echo(res);
        echo("Main thread completed");
    }

    public static void echo(String s) {
        System.out.println(s);
    }
}
