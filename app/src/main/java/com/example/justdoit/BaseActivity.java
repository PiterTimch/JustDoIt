package com.example.justdoit;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.justdoit.screens.AddTaskActivity;
import com.example.justdoit.screens.LoginActivity;
import com.example.justdoit.screens.MainActivity;
import com.example.justdoit.screens.RegisterActivity;
import com.example.justdoit.utils.CommonUtils;

public class BaseActivity extends AppCompatActivity {
    public BaseActivity() {
        CommonUtils.setContext(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int select = item.getItemId();

        if(select == R.id.m_create) {
            goToAddTask();
            return true;
        }

        if(select == R.id.m_zadachi) {
            goToMain();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void goToMain() {
        Intent intent = new Intent(BaseActivity.this, MainActivity.class);
        startActivity(intent);
    }

    protected void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    protected void goToRegistration() {
        Intent intent = new Intent(BaseActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    protected void goToAddTask() {
        Intent intent = new Intent(BaseActivity.this, AddTaskActivity.class);
        startActivity(intent);
    }


}
