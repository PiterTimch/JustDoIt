package com.example.justdoit;

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
import com.example.justdoit.config.Config;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.FileUtil;
import com.example.justdoit.utils.MyLogger;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends BaseActivity {

    private EditText titleInput;
    private ImageView imagePreview;
    private Uri selectedImageUri;

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
        setContentView(R.layout.activity_add_task);

        titleInput = findViewById(R.id.taskTitleInput);
        imagePreview = findViewById(R.id.taskImagePreview);

        findViewById(R.id.chooseImageButton)
                .setOnClickListener(v -> imagePicker.launch("image/*"));

        String url = Config.IMAGES_URL+"default.jpg";
        Glide.with(this)
                .load(url)
                .apply(new RequestOptions().override(300))
                .into(imagePreview);
    }

    public void onSaveClick(View view) {
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            MyLogger.toast(AddTaskActivity.this, "Введіть назву задачі");
            return;
        }
        if (selectedImageUri == null) {
            MyLogger.toast(AddTaskActivity.this, "Додайте зображення");
            return;
        }

        uploadTask(title, selectedImageUri);
    }

    private void uploadTask(String title, Uri imageUri) {
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        MultipartBody.Part imagePart = null;
        if(imageUri != null) {
            String imagePath = FileUtil.getImagePath(this, imageUri);
            if (imagePath != null) {
                File file = new File(imagePath);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
            }
        }

        RetrofitClient.getInstance()
                .getZadachiApi()
                .create(titlePart, imagePart)
                .enqueue(new Callback<ZadachaItemDTO>() {
                    @Override
                    public void onResponse(Call<ZadachaItemDTO> call, Response<ZadachaItemDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MyLogger.toast(AddTaskActivity.this, "Задача створена");
                            goToMain();
                        } else if (response.isSuccessful() && response.body() == null) {
                            Log.d("AddTaskActivity", "Response successful but body is null. Code: " + response.code());
                            MyLogger.toast(AddTaskActivity.this, "Задача створена");
                            goToMain();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("AddTaskActivity", "Server error: " + response.code() + ", body: " + errorBody);
                            MyLogger.toast(AddTaskActivity.this, "Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ZadachaItemDTO> call, Throwable t) {
                        Log.e("AddTaskActivity", "onFailure type: " + t.getClass().getName());
                        Log.e("AddTaskActivity", "message: " + t.getMessage(), t);
                        MyLogger.toast(AddTaskActivity.this, "Помилка: " + t.getMessage());
                    }
                });
    }
}
