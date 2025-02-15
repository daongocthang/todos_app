package com.standalone.todos;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.standalone.todos.databinding.ActivityMainBinding;
import com.standalone.todos.local.todos.TodoAdapter;

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

        if (NetworkUtil.isNetworkAvailable(this))
            syncInBackground();
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

}