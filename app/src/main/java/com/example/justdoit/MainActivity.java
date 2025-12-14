package com.example.justdoit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    RecyclerView taskRecycler;
    TaskAdapter adapter;
    View addButton;
    View deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taskRecycler = findViewById(R.id.taskRecycler);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);

        taskRecycler.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> goToAddTask());

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

    private void loadTasks() {
        RetrofitClient.getInstance().getZadachiApi().list().enqueue(new Callback<List<ZadachaItemDTO>>() {
            @Override
            public void onResponse(Call<List<ZadachaItemDTO>> call, Response<List<ZadachaItemDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new TaskAdapter(response.body(), hasSelected -> {
                        deleteButton.setVisibility(hasSelected ? View.VISIBLE : View.GONE);
                    });
                    taskRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ZadachaItemDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}

