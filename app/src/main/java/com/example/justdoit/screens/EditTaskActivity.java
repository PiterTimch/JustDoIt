package com.example.justdoit.screens;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.justdoit.BaseActivity;
import com.example.justdoit.R;
import com.example.justdoit.config.Config;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.CommonUtils;
import com.example.justdoit.utils.FileUtil;
import com.example.justdoit.utils.MyLogger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskActivity extends BaseActivity {

    private EditText titleInput;
    private ImageView imagePreview;

    private Uri selectedImageUri;
    private long taskId = -1;
    private String currentImageName;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imagePreview.setImageURI(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleInput = findViewById(R.id.editTaskTitleInput);
        imagePreview = findViewById(R.id.editTaskImagePreview);

        findViewById(R.id.editChooseImageButton)
                .setOnClickListener(v -> imagePicker.launch("image/*"));

        taskId = getIntent().getLongExtra("task_id", -1);
        currentImageName = getIntent().getStringExtra("task_image");
        String taskName = getIntent().getStringExtra("task_name");

        if (taskName != null) {
            titleInput.setText(taskName);
        }

        String url = currentImageName != null && !currentImageName.isEmpty()
                ? Config.IMAGES_URL + "200_" + currentImageName
                : Config.IMAGES_URL + "default.jpg";

        Glide.with(this)
                .load(url)
                .apply(new RequestOptions().override(300))
                .into(imagePreview);
    }

    public void onUpdateClick(View view) {

        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            MyLogger.toast("Введіть назву задачі");
            return;
        }

        if (taskId == -1) {
            MyLogger.toast("Немає ідентифікатора задачі");
            return;
        }

        updateTask(taskId, title, selectedImageUri);
    }

    private void updateTask(long id, String title, Uri imageUri) {

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            imagePart =
                    FileUtil.createImagePart(
                            this,
                            selectedImageUri,
                            "image",
                            "task.jpg"
                    );
            if (imagePart == null) {
                MyLogger.toast("Не вдалося підготувати зображення");
                return;
            }
        }

        CommonUtils.showLoading();

        RetrofitClient.getInstance()
                .getZadachiApi()
                .update(id, titlePart, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        CommonUtils.hideLoading();
                        if (response.isSuccessful()) {
                            MyLogger.toast("Задача оновлена");
                            goToMain();
                        } else {
                            MyLogger.toast("Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        CommonUtils.hideLoading();
                        Log.e("EditTaskActivity", "onFailure", t);
                        MyLogger.toast("Помилка: " + t.getMessage());
                    }
                });
    }
}
