package com.example.justdoit.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.justdoit.BaseActivity;
import com.example.justdoit.R;
import com.example.justdoit.TaskAdapter;
import com.example.justdoit.config.Config;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.CommonUtils;
import com.example.justdoit.utils.MyLogger;
import com.example.justdoit.utils.auth.SessionManager;
import com.example.justdoit.utils.auth.UserState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    RecyclerView taskRecycler;
    TaskAdapter adapter;
    View addButton;
    View deleteButton;
    View accountButton;

    private ImageView userAvatar; // Твоя іконка профілю

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taskRecycler = findViewById(R.id.taskRecycler);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        accountButton = findViewById(R.id.accountButton);
        userAvatar = findViewById(R.id.userAvatar);

        taskRecycler.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> goToAddTask());
        accountButton.setOnClickListener(v -> goToRegistration());

        userAvatar.setOnClickListener(v -> {
            new SessionManager(this).logout();
            UserState.getInstance().clear();
            updateAuthUI();
            MyLogger.toast("Ви вийшли з системи");
        });

        deleteButton.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.deleteSelectedItems(() -> {
                    deleteButton.setVisibility(View.GONE);
                    loadTasks();
                });
            }
        });

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAuthUI();
    }

    private void updateAuthUI() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token != null) {
            UserState user = UserState.getInstance();
            if (!user.isLoggedIn()) {
                user.setUserFromToken(token);
            }

            accountButton.setVisibility(View.GONE);
            userAvatar.setVisibility(View.VISIBLE);

            String imageUrl = Config.IMAGES_URL + "200_" + user.getImage();

            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(userAvatar);

        } else {
            accountButton.setVisibility(View.VISIBLE);
            userAvatar.setVisibility(View.GONE);
        }
    }

    private void loadTasks() {
        CommonUtils.showLoading();
        RetrofitClient.getInstance().getZadachiApi().list().enqueue(new Callback<List<ZadachaItemDTO>>() {
            @Override
            public void onResponse(Call<List<ZadachaItemDTO>> call, Response<List<ZadachaItemDTO>> response) {
                CommonUtils.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new TaskAdapter(response.body(), hasSelected -> {
                        deleteButton.setVisibility(hasSelected ? View.VISIBLE : View.GONE);
                    }, MainActivity.this::onClickEditZadacha);
                    taskRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ZadachaItemDTO>> call, Throwable t) {
                CommonUtils.hideLoading();
                t.printStackTrace();
            }
        });
    }

    private void onClickEditZadacha(ZadachaItemDTO zadacha) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task_id", zadacha.getId());
        intent.putExtra("task_name", zadacha.getName());
        intent.putExtra("task_image", zadacha.getImage());
        this.startActivity(intent);
    }
}