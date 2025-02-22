package com.standalone.todos;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.standalone.core.ext.ApiService;
import com.standalone.core.utils.Json;
import com.standalone.todos.databinding.ActivityMainBinding;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoAdapter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new TodoAdapter(this);
        binding.recycler.setAdapter(adapter);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FormDialog(adapter).show(getSupportFragmentManager(), FormDialog.TAG);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                syncInBackground();
            }
        }).start();
    }

    private void syncInBackground() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest serviceWorkRequest = new OneTimeWorkRequest.Builder(ServiceWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueue(serviceWorkRequest);
    }

    private void doInBackground() {
        final ApiService<Todo> apiService = new ApiService<>();
        if (NetworkUtil.isNetworkAvailable(this)) {
            try {
                Response res = apiService.fetchAll().get();
                if (res.body() == null) {
                    throw new IOException();
                }

                List<Todo> todoList = Json.parseList(res.body().string(), Todo.class);
                todoList.forEach(t -> {
                    System.out.println("Success: " + t.string());
                });

            } catch (ExecutionException | InterruptedException | IOException e) {
                System.out.println("Failure: " + e.getMessage());
            }
        }
    }

}