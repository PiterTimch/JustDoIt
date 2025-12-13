package com.example.justdoit;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    protected void goToMain() {
        Intent intent = new Intent(BaseActivity.this, MainActivity.class);
        startActivity(intent);
    }

    protected void goToAddTask() {
        Intent intent = new Intent(BaseActivity.this, AddTaskActivity.class);
        startActivity(intent);
    }
}
